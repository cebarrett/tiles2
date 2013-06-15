package models

import scala.collection._
import scala.util.control.Breaks._
import scala.util.Random
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

object World {
	def length:Int = 32;
	def lengthChunks = length
	def lengthTiles = lengthChunks * Chunk.length
	def clamp(n:Int):Int = Math.min(Math.max(0, n), World.lengthTiles-1);
}

/**
 * Holds the game world and simulates everything that happens in it.
 * To query or modify the state of the World, call one of its methods.
 * To run one tick of the simulation, call tick().
 * Worlds have an Enumerator[WorldEvent] that publishes interesting
 * events that happen. The world does not speak JSON and does not
 * communicate with individual players.
 */
class World {

	/** incremented once per tick */
	var ticks:Long = 0;

	/** Grid of all the chunks in the world */
	val chunkGrid = new ChunkGrid

	/**
	 * Emits WorldEvent to notify listeners when things happen in the world.
	 * Note: World should not send messages to specific players.
	 */
	val (eventEnumerator, eventChannel) = Concurrent.broadcast[WorldEvent]

	/** List of all players in the world, keyed by name */
	var players = Map.empty[String,Player]

	def chunkAt(cx:Int, cy:Int):Chunk = chunkAt(ChunkCoordinates(cx,cy))

	def chunkAt(coords:ChunkCoordinates):Chunk = chunkGrid.getOrGenerate(coords)

	def tileAt(coords:WorldCoordinates):Tile = tileAt(coords.x, coords.y)

	def tileAt(x:Int, y:Int):Tile = {
		return chunkAt(WorldCoordinates(x, y).toChunkCoordinates()).tileAt(x,y)
	}

	def entity(coords:WorldCoordinates):Option[Entity] = tileAt(coords).entity


	/*
	 * Run 1 tick of the game loop.
	 * note: iterates over every tile in the game,
	 * which may not be scalable.
	 * if regions need to be more than 512x512, or
	 * if tick needs to run more often than 1 second,
	 * will need to schedule tile ticks instead.
	 *
	 * FIXME: this can move the same llama multiple times per tick,
	 * because it iterates over tiles, not entities, and does not
	 * remember which entities have already moved.
	 */
	def tick():Unit = {
		chunkGrid.foreach { entry =>
			val (chunkCoords, chunk) = entry
			chunk.tiles foreach { tcol =>
				tcol foreach { t =>
					val coords = TileCoordinates(t.tx, t.ty).toWorldCoordinates(chunkCoords)
					t.entity.map {_.tick(this, coords)}
				}
			}
		}
		ticks = ticks + 1;
	}

	def spawnPlayer(playerName:String):Player = {
		// determine a suitable spawn location - pretty basic for now
		var tile:Tile = null
		var (x, y) = (0, 0)
		breakable { for (x <- 0 until 500) {
			for (y <- 0 until 500) {
				tile = tileAt(x, y)
				if (tile.entity.isEmpty) break
			}
		}}
		// create a player object and hold a reference
		val player = new Player(playerName, x, y)
		players = players + (playerName -> player)
		// spawn a player entity
		val playerEntity = (tile.entity = Some(new EntityPlayer(player)))
		// broadcast entity spawn
		this.eventChannel.push(WorldEvent("playerSpawn", Some(x), Some(y), Some(tile), Some(player)))
		return player
	}

	def despawnPlayer(playerName:String):Unit = {
		players.get(playerName) match {
			case Some(player) =>
				val (x, y) = (player.x, player.y)
				val tile = tileAt(x, y)
				// remove the player entity
				tile.entity = None
				// null out reference to the player object
				players = players - playerName
				// broadcast entity despawn
				this.eventChannel.push(WorldEvent("playerDespawn", Some(x), Some(y), Some(tile), Some(player)))
			case None =>
				// FIXME: there is a bug where this happens if i open around 50
				// browser tabs w/ the game open then close them all at the same time.
				Logger.warn(s"Tried to despawn player who is not logged in: $playerName")
		}
	}

	def despawnEntity(coords:WorldCoordinates):Option[Entity] = {
		val tile = tileAt(coords)
		val entity = tile.entity;
		tile.entity = None;
		this.eventChannel.push(WorldEvent("entityDespawn", Some(coords.x), Some(coords.y), Some(tile)))
		return entity
	}


	def movePlayer(playerName:String, dx:Int, dy:Int):Unit = {
		players.get(playerName) map { player =>
			val oldX:Int = player.x
			val oldY:Int = player.y
			val (newX, newY) = (oldX+dx, oldY+dy)
			if (newX < 0 || newX >= World.lengthTiles || newY < 0 || newY >= World.lengthTiles) return
			val oldTile = tileAt(oldX, oldY)
			val newTile = tileAt(newX, newY)
			(newTile.entity.isEmpty) match {
				case true => {
					// No entity occupying the tile so move there.
					moveEntity(WorldCoordinates(oldX, oldY), WorldCoordinates(newX, newY))
				} case false => {
					// An entity is occupying this tile so interact with it.
					doEntityInteraction(WorldCoordinates(oldX,oldY), WorldCoordinates(newX,newY))
				}
			}
		}
	}

	def killPlayer(player:Player):Unit = {
		despawnPlayer(player.name)
	}

	def moveEntity(oldCoords:WorldCoordinates, newCoords:WorldCoordinates):Unit = {
		val (oldTile, newTile) = (tileAt(oldCoords), tileAt(newCoords))
		val (oldEntity, newEntity) = (oldTile.entity, newTile.entity)
		// require(oldEntity.isDefined)

		newEntity.getOrElse {
			newTile.entity = oldEntity
			oldTile.entity = None

			newTile.entity.map {
				case playerEntity:EntityPlayer => {
					val player = players.get(playerEntity.player.name).get
					player.x = newCoords.x
					player.y = newCoords.y
					val event = WorldEvent("entityMove", Some(newCoords.x), Some(newCoords.y), Some(newTile), Some(player), None, Some(oldCoords.x), Some(oldCoords.y))
					eventChannel.push(event)
				}
				case _:Any => {
					val event = WorldEvent("entityMove", Some(newCoords.x), Some(newCoords.y), Some(newTile), None, None, Some(oldCoords.x), Some(oldCoords.y))
					eventChannel.push(event)
				}
			}
		}
	}
	
	def doPlayerCrafting(playerName:String, recipe:WorkbenchRecipe):Unit = {
		val player:Player = players.get(playerName).get
		// validate that the player has enough items to make the recipe
		var hasIngredients:Boolean = true
		for (i:Item <- recipe.ingredients) {
			if (player.inventory.has(i) == false) {
				hasIngredients = false
			}
		}
		if (hasIngredients) {
			val playerTile:Tile = tileAt(player.x, player.y)
			player.inventory.add(recipe.result);
			recipe.ingredients.map {player.inventory.subtract(_)}
			this.eventChannel.push(WorldEvent("playerCraft", Some(player.x), Some(player.y), Some(playerTile), Some(player)))
		} else {
			Logger.debug(s"Player $playerName did not have the ingredients to craft $recipe")
		}
	}

	def doEntityInteraction(attackerCoords:WorldCoordinates, targetCoords:WorldCoordinates):Unit = {

		val (attackerTile:Tile, targetTile:Tile) = (tileAt(attackerCoords), tileAt(targetCoords))
		val attackerEntity:EntityLiving = attackerTile.entity.get.asInstanceOf[EntityLiving]
		val targetEntity:Entity = entity(targetCoords).head
		val roll:Double = Random.nextDouble

		// actions any living entities can do
		if (targetEntity.isInstanceOf[EntityLiving]) {
			val target:EntityLiving = targetEntity.asInstanceOf[EntityLiving]
			val hit:Boolean = attackerEntity.attack(target)
			if (hit) {
				// broadcast an update for both tiles
				broadcastTileEvent(attackerCoords)
				broadcastTileEvent(targetCoords)
				if (target.dead) {
					if (target.isInstanceOf[EntityMob]) {
						despawnEntity(targetCoords)
					}
					if (target.isInstanceOf[EntityPlayer]) {
						killPlayer(target.asInstanceOf[EntityPlayer].player)
					}
				}
			}
		}

		// actions only players can do
		else {
			/* FIXME: caused a ClassCastException because attackerEntity was a goblin */
			val player:Player = players.get(attackerEntity.asInstanceOf[EntityPlayer].player.name).getOrElse(null)
			targetEntity match {
			/*
			 * FIXME: this is getting very repetitive!
			 */
			case (target:EntityTree) => {
				if (player isHoldingItem "axe") {
					player.inventory add Item("log", Some(1))
					player.inventory.add(Item("sapling", Some(Random.nextInt(4))))
					despawnEntity(targetCoords)
				} else {
					if (roll < 0.05) {
						player.inventory.add(Item("wood", Some(1)))
						despawnEntity(targetCoords)
					}
				}
			}
			case (target:EntityWorkbench) => {
				if (player isHoldingItem "hammer") {
					player.inventory add Item("workbench", Some(1))
					despawnEntity(targetCoords)
				} else {
					/*
					 *	FIXME: World should not be broadcasting the list of recipes,
					 * just the fact that the player is using the workbench
					 * (or other crafting type entity).
					 * Instead send recipes from Game when the player logs in.
					 */
					val options:Seq[String] = "Close" +: WorkbenchRecipe.ALL.map({_.toString})
					this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
				}
			}
			case (target:EntityKiln) => {
				if (player isHoldingItem "hammer") {
					player.inventory add Item("kiln", Some(1))
					despawnEntity(targetCoords)
				} else {
					val options:Seq[String] = "Close" +: KilnRecipe.ALL.map({_.toString})
					this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
				}
			}
			case (target:EntitySmelter) => {
				if (player isHoldingItem "hammer") {
					player.inventory add Item("smelter", Some(1))
					despawnEntity(targetCoords)
				} else {
					val options:Seq[String] = "Close" +: SmelterRecipe.ALL.map({_.toString})
					this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
				}
			}
			case (target:EntitySawmill) => {
				if (player isHoldingItem "hammer") {
					player.inventory add Item("sawmill", Some(1))
					despawnEntity(targetCoords)
				} else {
					val options:Seq[String] = "Close" +: SawmillRecipe.ALL.map({_.toString})
					this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
				}
			}
			case (target:EntityStonecutter) => {
				if (player isHoldingItem "hammer") {
					player.inventory add Item("stonecutter", Some(1))
					despawnEntity(targetCoords)
				} else {
					val options:Seq[String] = "Close" +: StonecutterRecipe.ALL.map({_.toString})
					this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
				}
			}
			case (target:EntityStone) => {
				if ((player isHoldingItem "pick") && (Random.nextDouble < 0.5)) {
					despawnEntity(targetCoords)
					if (Math.random < 0.5) {
						player.inventory add Item("rock", Some(1), Some(target.material))
					}
				}
			}
			case (target:EntityOre) => {
				if ((player isHoldingItem "pick") && (Random.nextDouble < 0.25)) {
					player.inventory add Item("ore", Some(1), Some(target.material))
					despawnEntity(targetCoords)
				}
			}
			case (_) => Unit
		}
		this.eventChannel.push(WorldEvent("playerUpdate", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player)))
		}
	}
	
	def doPlaceItem(playerName:String, target:WorldCoordinates) = {
		players.get(playerName).map { player =>
			// FIXME: verify the target is within N blocks of player
			player.inventory.selected map { itemIndex =>
				if (itemIndex >= 0 && itemIndex < player.inventory.items.length) {
					val targetTile = tileAt(target)
					targetTile.entity.getOrElse {
						val item:Item = player.inventory.items(player.inventory.selected.get)
						(item.kind) match {
							// FIXME: this is getting repetitive
							case "sapling" => 
								player.inventory.subtract(Item("sapling", Some(1)))
								targetTile.entity = Some(EntitySapling())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case "workbench" => 
								player.inventory.subtract(Item("workbench", Some(1)))
								targetTile.entity = Some(EntityWorkbench())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case "kiln" => 
								player.inventory.subtract(Item("kiln", Some(1)))
								targetTile.entity = Some(EntityKiln())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case "smelter" => 
								player.inventory.subtract(Item("smelter", Some(1)))
								targetTile.entity = Some(EntitySmelter())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case "sawmill" => 
								player.inventory.subtract(Item("sawmill", Some(1)))
								targetTile.entity = Some(EntitySawmill())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case "stonecutter" => 
								player.inventory.subtract(Item("stonecutter", Some(1)))
								targetTile.entity = Some(EntityStonecutter())
								this.eventChannel.push(WorldEvent("placeBlock", Some(target.x), Some(target.y), Some(targetTile), Some(player)))
							case _ => Unit
						}
					}
				}
			}
		}
	}

	def doSelectItem(playerName:String, inventoryIndex:Int) = {
		val player:Player = players.get(playerName).get
		if (inventoryIndex < 0 || inventoryIndex >= player.inventory.items.size) {
			// invalid index
		} else {
			player.inventory.selected = Some(inventoryIndex)
			this.eventChannel.push(WorldEvent("playerSelect", Some(player.x), Some(player.y), Some(tileAt(player.x,player.y)), Some(player)))
		}
	}

	private def broadcastTileEvent(pos:WorldCoordinates):Unit = {
		val tile:Tile = tileAt(pos)

		val player:Option[Player] = tileAt(pos).entity match {
			case Some(entity:EntityPlayer) => (players get entity.player.name)
			case _ => None
		}

		val event:WorldEvent = WorldEvent("tile", Some(pos.x), Some(pos.y), Some(tile), player)

		this.eventChannel.push(event)
	}
}

case class WorldEvent(
	val kind:String,
	val x:Option[Int] = None,
	val y:Option[Int] = None,
	val tile:Option[Tile] = None,
	val player:Option[Player] = None,
	val options:Option[Seq[String]] = None,
	val prevX:Option[Int] = None,
	val prevY:Option[Int] = None
)
