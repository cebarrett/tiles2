package models

import scala.collection._
import scala.util.control.Breaks._
import scala.util.Random
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

object World {
	def radius:Int = 32;
	def radiusChunks = radius;
	def radiusTiles = radius * Chunk.length
	def clamp(n:Int):Int = Math.min(Math.max(-radiusTiles, n), radiusTiles-1);
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
		val chunk = chunkAt(WorldCoordinates(x, y).toChunkCoordinates())
		return chunk.tileAt(x,y)
	}

	def entity(coords:WorldCoordinates):Option[Entity] = tileAt(coords).entity

	/** Run 1 tick of the game loop. */
	def tick():Unit = {
		// TODO: this builds an array of ~500k entities every 1s... too slow
		var allEntityCoords = Seq.empty[(Entity, WorldCoordinates)]
		chunkGrid.foreach { entry =>
			val (chunkCoords, chunk) = entry
			chunk.tiles foreach { tcol =>
				tcol foreach { t =>
					t.entity map { e =>
						val coords = TileCoordinates(t.tx, t.ty).toWorldCoordinates(chunkCoords)
						allEntityCoords = (e, coords) +: allEntityCoords
					}
				}
			}
		}
		Logger trace s"entities to tick: ${allEntityCoords.length}"
		allEntityCoords foreach { entry =>
			val (e, pos) = entry
			// XXX: precondition: the entity has not moved since the previous loop
			val tile = (this tileAt pos)
			if (tile.entity.isEmpty || tile.entity.get != e) {
				throw new RuntimeException("entity moved before its turn")
			}
			e.tick(this, pos)
		}
		ticks = ticks + 1;
	}
	
	/** Pre-load all of the chunks in the world. */
	def loadAllChunks():World = {
		Logger.debug("Loading all chunks")
		var chunkCount:Int = 0
		for (cx <- (-World.radius) until World.radius) {
			for (cy <- (-World.radius) until World.radius) {
				chunkGrid.getOrGenerate(ChunkCoordinates(cx, cy))
				chunkCount = chunkCount + 1
			}
		}
		Logger.debug(s"Done, loaded $chunkCount chunks")
		this
	}
	
	def connectPlayer(playerName:String):Player = {
		Logger debug s"connect: $playerName"
		// create a player object and hold a reference
		val player = new Player(playerName, 0, 0)
		players = players + (playerName -> player)
		player
	}

	def spawnPlayer(playerName:String):Unit = {
		Logger debug s"spawn: $playerName"
		val player = players get playerName get
		// determine a suitable spawn location - pretty basic for now
		var tile:Tile = null
		var spawnPos:WorldCoordinates = null
		breakable { for (y <- (player.y) until (player.y+100)) {
			for (x <- (player.x) until (player.x+100)) {
				tile = tileAt(x, y)
				spawnPos = WorldCoordinates(x, y)
				if (tile.entity.isEmpty) break
			}
		}}
		// spawn a player entity and update the player object
		val playerEntity = (tile.entity = Some(new EntityPlayer(player)))
		player.x = spawnPos.x;
		player.y = spawnPos.y;
		// broadcast entity spawn
		this.eventChannel.push(WorldEvent("playerSpawn", Some(spawnPos.x), Some(spawnPos.y), Some(tile), Some(player)))
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
			if (newX < -World.radiusTiles || newX >= World.radiusTiles || newY < -World.radiusTiles || newY >= World.radiusTiles) return
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

	/**
	 * Move an entity from oldCoords to newCoords and handle updating other
	 * world state and firing events accordingly.
	 * Preconditions: there is an entity at oldCoords and none at newCoords.
	 */
	def moveEntity(oldCoords:WorldCoordinates, newCoords:WorldCoordinates):Unit = {

		val (oldTile:Tile, newTile:Tile) = (tileAt(oldCoords), tileAt(newCoords))

		val (oldEntity:Option[Entity], newEntity:Option[Entity]) = (oldTile.entity, newTile.entity)

		if (oldEntity.isEmpty) {
			Logger warn s"Tried to move entity in empty tile at $oldCoords"
			return
		}
		if (newEntity.isDefined) {
			Logger warn s"Tried to move entity at $oldCoords into occupied tile at $newCoords"
			return
		}

		newTile.entity = oldEntity
		oldTile.entity = None
		newTile.entity.get match {
			case playerEntity:EntityPlayer => {
				val player = players.get(playerEntity.player.name).get
				player.x = newCoords.x
				player.y = newCoords.y
				val event = WorldEvent("entityMove", Some(newCoords.x), Some(newCoords.y), Some(newTile), Some(player), Some(oldCoords.x), Some(oldCoords.y))
				eventChannel.push(event)
			}
			case _:Any => {
				val event = WorldEvent("entityMove", Some(newCoords.x), Some(newCoords.y), Some(newTile), None, Some(oldCoords.x), Some(oldCoords.y))
				eventChannel.push(event)
			}
		}
	}

	def doPlayerCrafting(playerName:String, kind:String, index:Int):Unit = {
		players get playerName map { player:Player =>
			doPlayerCrafting(player, Recipe.kind(kind)(index))
		}
	}

	def doPlayerCrafting(player:Player, recipe:Recipe):Unit = {
		if (recipe craft player.inventory) {
			this.broadcastTileEvent(WorldCoordinates(player.x, player.y))
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
				// XXX: this behavior belongs in the entity subclass
				case (target:EntityTree) => {
					if (player isHoldingItem "axe") {
						player.inventory add ItemStack(EntityBlock(Wood), Some(1))
						player.inventory add ItemStack(EntitySapling(), Some(Random.nextInt(2)+1))
						despawnEntity(targetCoords)
					}
				}
				case _:EntityWorkbench | _:EntityKiln | _:EntitySmelter |
						_:EntitySawmill | _:EntityStonecutter => {
					if (player isHoldingItem "hammer") {
						despawnEntity(targetCoords) map {
							player.inventory add ItemStack(_, Some(1))
						}
					}
				}
				case (target:EntityBlock) => {
					if (player isHoldingItem "pick") {
						despawnEntity(targetCoords) map {
							player.inventory add ItemStack(_, Some(1))
						}
					}
				}
				case (_) => Unit
			}
			this.eventChannel.push(WorldEvent(
					"playerUpdate",
					Some(attackerCoords.x),
					Some(attackerCoords.y),
					Some(attackerTile),
					Some(player)))
		}
	}
	
	/**
	 * Places the player's currently selected item at the given coordinates
	 * if the player is within 20 blocks.
	 * 
	 * @return true if the item was placed, false if not.
	 */
	def doPlaceItem(playerName:String, target:WorldCoordinates):Boolean = {
		players.get(playerName).map { player =>
			if (player.pos.distanceTo(target) > 10) {
				return false
			} else {
				player.inventory.selected map { itemIndex =>
					if (itemIndex >= 0 && itemIndex < player.inventory.items.length) {
						val targetTile = tileAt(target)
						targetTile.entity map {_ => true} getOrElse {
							player getSelectedItem() map { stack =>
								stack.item match {
									case entity:Entity => {
										player.inventory.subtractOneOf(stack)
										targetTile.entity = Some(entity)
										broadcastTileEvent(target)
										broadcastPlayer(player)
										true
									}
									case _ => false
								}
							} getOrElse false
						}
					} else false
				} getOrElse false
			}
		} getOrElse false
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

	def broadcastTileEvent(pos:WorldCoordinates):Unit = {
		val tile:Tile = tileAt(pos)

		val player:Option[Player] = tileAt(pos).entity match {
			case Some(entity:EntityPlayer) => (players get entity.player.name)
			case _ => None
		}

		val event:WorldEvent = WorldEvent("tile", Some(pos.x), Some(pos.y), Some(tile), player)

		this.eventChannel.push(event)
	}
	
	def broadcastPlayer(player:Player):Unit = {
		val tile:Option[Tile] = Option(tileAt(player.x, player.y))
		val event:WorldEvent = WorldEvent(
				"player", Some(player.x), Some(player.y), tile, Some(player)) 
		this.eventChannel.push(event)
	}
}

case class WorldEvent(
	val kind:String,
	val x:Option[Int] = None,
	val y:Option[Int] = None,
	val tile:Option[Tile] = None,
	val player:Option[Player] = None,
	val prevX:Option[Int] = None,
	val prevY:Option[Int] = None
)
