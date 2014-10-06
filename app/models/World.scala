package models

import scala.collection._
import scala.util.control.Breaks._
import scala.util.Random
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

object World {
	val monsterCap = World.tileCount / 400
	def radius:Int = 32
	def radiusChunks = radius;
	def radiusTiles = radius * Chunk.length
	def lengthChunks = radiusChunks * 2 + 1
	def lengthTiles = radiusTiles * 2 + 1
	def chunkCount = math.pow(lengthChunks, 2).toInt
	def tileCount = math.pow(lengthTiles, 2).toInt
	def clamp(n:Int):Int = Math.min(Math.max(-radiusTiles, n), radiusTiles-1)
	def ticksPerDay:Long = 3600
	def randomXorY = -radiusTiles + util.Random.nextInt(lengthTiles)
	def randomCoordinates = WorldCoordinates(randomXorY, randomXorY)
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
	var ticks = World.ticksPerDay / 3

	/** Grid of all the chunks in the world */
	private val chunkGrid = new ChunkGrid

	/** Emits WorldEvent when things happen in the world. */
	val (eventEnumerator, eventChannel) = Concurrent.broadcast[WorldEvent]

	/** Cache of positions of entities in the world.
	 *  Does not contain all entities, just the ones to tick every turn. */
	private val entityCache = new WorldEntityCache
	
	/** All players in the world, logged in or not */
	val players = new mutable.HashSet[Player] {
		def get(name:String):Option[Player] = find {_.name == name}
	}

	/**
	 * Run 1 tick of the game loop.
	 */
	def tick():Unit = {
		ticks = ticks + 1

		// FIXME: delete or fix next line, never runs if game starts > 0
		if (ticks == 1L) Logger info "Started game loop"

		// Run a tick for each entity in the entity cache.
		entityCache.all foreach { obj =>
			val worldEntity:WorldEntity = obj.asInstanceOf[WorldEntity]
			val entity:Entity = worldEntity.entity.asInstanceOf[Entity]
			entity match {
				case _ => {
					try {
						val pos:WorldCoordinates = entityCache.get(worldEntity).get
						val tile = tileAt(pos)
						if (tile.entity.isEmpty || tile.entity.get != entity) {
							Logger warn s"entity not found where it was expected: ${worldEntity}"
							entityCache remove worldEntity
						} else {
							entity.tick(this, pos)
						}
					} catch {
						case _:Exception => Logger warn s"entity not found in cache: ${worldEntity}"
					}
				}
			}
		}
		
		// If it's night and the world is below the monster cap then spawn monsters.
		if (isNight && entityCache.monsters.size < World.monsterCap) {
			val numMobsToSpawn = World.monsterCap / 200
			for (i <- 0 until numMobsToSpawn) {
				val pos = World.randomCoordinates
				if (!isPlayerNear(pos)) {
					val tile = tileAt(pos)
					if (tile.terrain.spawnMonsters && tile.entity.isEmpty) {
						val entity = Math.random match {
							case i if i < 0.1 => new EntityOrc()
							case i if i < 0.4 => new EntityGoblin()
							case _ => new EntitySpider()
						}
						spawnEntity(entity, pos)
					}
				}
			}
		}
	}
	
	def isPlayerNear(pos:WorldCoordinates, radius:Int = Chunk.length*2):Boolean =
		entityCache.players.map(entityCache.get(_)).filter(_.isDefined)
				.map(_.get._2)
				.filter(_.distanceTo(pos) < radius)
				.nonEmpty

	/**
	 * Find an entity in the world.
	 * FIXME: should this return Option[T]?
	 */
	def find[T <: Entity](entity:T):Option[WorldEntity] = {
		entityCache get entity map {_._1}
	}
	
	def find(player:Player):Option[WorldEntity] = entityCache get player map {_._1}
	
	/** Find an entity's position in the world. */
	def pos(e:Entity):Option[WorldCoordinates] = (this find e).map(entityCache get _).getOrElse(None)
	
	/** Get or create the player with the given name */
	def fetchPlayer(playerName:String):Player = {
		players find {_.name == playerName} getOrElse {
			val player = new Player(playerName)
			giveNewPlayerItems(player)
			players.add(player)
			player
		}
	}
	
	def giveNewPlayerItems(player:Player):Unit =
		player.give(if (Game.DEV) Seq(
			new ItemStack(new EntityBlock(Wood), Some(1000)),
			new ItemStack(new EntityBlock(Obsidian), Some(1000)),
			new ItemStack(new EntityBlock(Germanium), Some(1000)),
			new ItemStack(new EntityWorkbench(Diamond), Some(100)),
			new ItemStack(new Food(), Some(100)),
			new ItemStack(new Armor(Wood)),
			new ItemStack(new Axe(Wood)),
			new ItemStack(new Pick(Wood)),
			new ItemStack(new Hammer(Wood)),
			new ItemStack(new Armor(Diamond)),
			new ItemStack(new Sword(Diamond)),
			new ItemStack(new Axe(Diamond)),
			new ItemStack(new Pick(Diamond)),
			new ItemStack(new Hammer(Diamond))
		) else Seq(
			new ItemStack(new Axe(Wood)),
			new ItemStack(new EntityWorkbench(Wood))
		))

	/** Gets the time of day in hours, a value from 0 to 24. */
	def time = 24 * ((ticks % World.ticksPerDay).toDouble/World.ticksPerDay.toDouble)
	
	/** Get just the hours part of the time, an int from 0 to 24. */
	def hours = time.toInt
	
	/** Get just the minutes part of the time, an int from 0 to 60. */
	def minutes = (60 * (time - hours)).toInt
	
	def isNight = (hours >= 18 || hours <= 5)
	
	/** Gets the time of day as a string HH:MM */
	def timeStr = "%2d:%2d".format(hours, minutes).replaceAll(" ", "0")

	/** Get or generate a chunk. */
	def chunkAt(coords:ChunkCoordinates):Chunk = {
		val lenBefore = chunkGrid.size
		val chunk = chunkGrid.getOrGenerate(coords)
		val lenAfter = chunkGrid.size
		// if a chunk was generated, put its entities into the position cache
		// (all entities except blocks)
		if (lenAfter - lenBefore == 1) {
			addEntitiesToCache(chunk)
		}
		chunk
	}
	
	/** Add all entities in a chunk to the entity cache. */
	private def addEntitiesToCache(chunk:Chunk):Unit = {
		chunk.tiles foreach { tcol =>
			tcol foreach { tile =>
				tile.entity map { entity =>
					if (shouldAddEntityToCache(entity)) {
						val pos = tile.coords.pos(chunk.pos)
						val worldEntity = new WorldEntity(entity, this)
						entityCache.put(worldEntity, pos)
					}
				}
			}
		}
	}
	
	private def shouldAddEntityToCache(entity:Entity):Boolean = {
		entity match {
			case _:EntityBlock | _:EntityTree => false
			case _ => true
		}
	}

	def chunkAt(cx:Int, cy:Int):Chunk = chunkAt(ChunkCoordinates(cx,cy))

	def tileAt(coords:WorldCoordinates):Tile = tileAt(coords.x, coords.y)

	def tileAt(x:Int, y:Int):Tile = {
		val chunk = chunkAt(WorldCoordinates(x, y).toChunkCoordinates())
		return chunk.tileAt(x,y)
	}

	def entity(coords:WorldCoordinates):Option[Entity] = tileAt(coords).entity
	
	/** Iterate over every tile in the world (slow) */
	def forEachTile[B](fn:((Tile, WorldCoordinates) => B)):Unit = {
		chunkGrid.foreach { entry =>
			val (chunkCoords, chunk) = entry
			val hasPlayersNearby = players map { player =>
				find(player) map { worldEntity =>
					(Chunk.length * 6) > chunk.pos.toWorldCoordinates.distanceTo(worldEntity.pos)
				} getOrElse false
			} exists {_ == true};
			if (hasPlayersNearby) {
				chunk.tiles foreach { tcol =>
					tcol foreach { t =>
						val pos = TileCoordinates(t.tx, t.ty).toWorldCoordinates(chunkCoords)
						fn(t, pos)
					}
				}
			}
		}
	}
	
	/** Pre-load all of the chunks in the world. */
	def loadAllChunks():World = {
		Logger.info("Loading all chunks")
		var chunkCount:Int = 0
		for (cx <- (-World.radius) until World.radius) {
			for (cy <- (-World.radius) until World.radius) {
				val chunk = chunkGrid.getOrGenerate(ChunkCoordinates(cx, cy))
				addEntitiesToCache(chunk)
				chunkCount = chunkCount + 1
			}
		}
		Logger.info(s"Done, loaded $chunkCount chunks")
		this
	}

	/** Spawn a player. Handles updating other world state and broadcasting the event. */
	def spawnPlayer(playerName:String):Unit = {
		val player = fetchPlayer(playerName)
		val spawnPos = findSpawnPositionForPlayer(player) getOrElse {
			Logger.error("Could not find a vacant spawn position")
			return
		}
		val spawnTile = tileAt(spawnPos)
		val playerEntity = new EntityPlayer(player)
		spawnTile.entity = Some(playerEntity)
		val worldEntity = new WorldEntity(playerEntity, this)
		entityCache.put(worldEntity, spawnPos);
		broadcastTileEvent(spawnPos)
	}
	
	def findSpawnPositionForPlayer(player:Player):Option[WorldCoordinates] = {
		// if player has a spawn position set, spawn them in an adjacent tile,
		// otherwise spawn them somewhere near the world spawn
		player.spawn.map(_.getAdjacent.filter(tileAt(_).entity.isEmpty).headOption)
				.getOrElse(findRandomPositionNearSpawn)
	}
	
	def findRandomPositionNearSpawn():Option[WorldCoordinates] = {
		val spawn = WorldCoordinates(0,0)
		var tries = 0
		while (tries < 500) {
			val c = spawn.randomCoordsInRadius(10)
			if (tileAt(c).entity.isEmpty) {
				return Some(c)
			} else {
				tries = tries + 1
			}
		}
		None
	}
	
	def spawnEntity(entity:Entity, coords:WorldCoordinates):Unit = {
		val tile = tileAt(coords)
		tile.entity.map({ currTileEntity =>
			Logger warn s"Spawning entity on top of another entity: ${currTileEntity}"
		}).getOrElse({
			tile.entity = Option(entity)
			if (shouldAddEntityToCache(entity)) {
				val worldEntity = new WorldEntity(entity, this)
				entityCache.put(worldEntity, coords)
			}
			broadcastTileEvent(coords)
		})
	}

	/** Remove the entity from a tile and broadcast the event. */
	def despawnEntity(coords:WorldCoordinates):Option[Entity] = {
		val tile = tileAt(coords)
		tile.entity map { entity =>
			tile.entity = None
			find(entity) map {entityCache.remove(_)}
			broadcastTileEvent(coords)
			entity
		}
	}

	/**
	 * Despawn the entity at coords and replace it with the given entity.
	 * Assumes the given entity is not currently spawned.
	 */
	def replaceEntity(entity:Entity, coords:WorldCoordinates):Unit = {
		val tile = tileAt(coords)
		tile.entity map { oldEntity =>
			Logger info s"replacing entity ${oldEntity}"
			tile.entity = None
			find(oldEntity) map {entityCache.remove(_)}
			spawnEntity(entity, coords)
		}
	}

	def movePlayer(playerName:String, dx:Int, dy:Int):Unit = {
		players.get(playerName) map { player =>
			find(player) map { worldEntity =>
				val pos = worldEntity.pos
				val (oldX, oldY) = (pos.x, pos.y)
				val (newX, newY) = (oldX+dx, oldY+dy)
				if (newX < -World.radiusTiles || newX >= World.radiusTiles || newY < -World.radiusTiles || newY >= World.radiusTiles) {
					return
				}
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
			} getOrElse {
				Logger warn s"Tried to move player $playerName who is not spawned"
			}
		} getOrElse {
			Logger warn s"Tried to move nonexistent player $playerName"
		}
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

		val entity = oldEntity.get
		
		newTile.entity = Some(entity)
		oldTile.entity = None
		val player:Option[Player] = entity match {
			case playerEntity:EntityPlayer => Some(playerEntity.player)
			case _ => None
		}
		find(newTile.entity.get) map { worldEntity =>
			entityCache.put(worldEntity, newCoords)
			val event = WorldEvent(timeStr, "entityMove", Some(newCoords.x), Some(newCoords.y), Some(newTile), player, Some(oldCoords.x), Some(oldCoords.y))
			eventChannel.push(event)
		} getOrElse {
			Logger warn s"Moved entity $entity that is not in the position cache"
		}
	}

	def doPlayerCrafting(playerName:String, kind:String, index:Int):Unit =
		doPlayerCrafting(fetchPlayer(playerName), Recipe.kind(kind)(index))

	def doPlayerCrafting(player:Player, recipe:Recipe):Unit =
		if (recipe craft player.inventory)
			this.broadcastPlayer(player)
	
	// FIXME: there are two despawnPlayer methods that do different things
	def despawnPlayer(player:Player):Unit = {
		find(player) map { worldEntity =>
			val pos = worldEntity.pos
			val (x, y) = (pos.x, pos.y)
			val tile = tileAt(x, y)
			// remove the player entity
			despawnEntity(WorldCoordinates(x, y))
			// broadcast entity despawn. frontend looks for an event with this message name.
			this.eventChannel.push(WorldEvent(timeStr, "playerDespawn", Some(x), Some(y), Some(tile), Some(player)))
		}
	}

	/**
	 * Does interaction between attacker and target and broadcasts events.
	 */
	def doEntityInteraction(attackerCoords:WorldCoordinates, targetCoords:WorldCoordinates):Unit = {

		val (attackerTile:Tile, targetTile:Tile) = (tileAt(attackerCoords), tileAt(targetCoords))
		val attackerEntity:EntityLiving = {
			attackerTile.entity map {
				_ match {
					case entity:EntityLiving => entity
					case _ => {
						Logger warn "doEntityInteraction: non-living entity tried to attack"
						return
					}
				}
			} getOrElse {
				Logger warn "doEntityInteraction: no entity found at given attacker coords"
				return
			}
		}
		val targetEntity:Entity = entity(targetCoords).getOrElse({
			Logger warn "doEntityInteraction: no entity found at given target coords"
			return
		})
		val roll:Double = Random.nextDouble

		// any living entity can target a living entity
		if (targetEntity.isInstanceOf[EntityLiving]) {
			val target:EntityLiving = targetEntity.asInstanceOf[EntityLiving]
			val hit:Boolean = attackerEntity.attack(target)
			if (hit) {
				if (target.dead) {
					if (target.isInstanceOf[EntityMob]) {
						despawnEntity(targetCoords)
					}
					if (target.isInstanceOf[EntityPlayer]) {
						despawnPlayer(target.asInstanceOf[EntityPlayer].player)
					}
					// give player a dead mob's drops
					if (attackerEntity.isInstanceOf[EntityPlayer]) {
						// give player the dead entity's drops
						val pe = attackerEntity.asInstanceOf[EntityPlayer]
						pe.player.give(target.drops)
					}
				}
				// broadcast an update for both tiles
				broadcastTileEvent(attackerCoords)
				broadcastTileEvent(targetCoords)
			}
		} else {
			// only players can target a non-living entity for now
			// because only players have inventories
			attackerEntity match {
				case entityPlayer:EntityPlayer => {
					val player = entityPlayer.player
					val tool:Option[Tool] = player.getSelectedTool
					if (targetEntity canBeBrokenBy tool) {
						// if entity can be broken by nothing, always succeeds,
						// otherwise have the tool roll to check whether it succeeds
						if (tool map {_.tryToBreak(targetEntity)} getOrElse {true}) {
							despawnEntity(targetCoords) map { entity =>
								val drops = entity.drops
								if (drops.nonEmpty) {
									player.give(drops)
								}
								// XXX: hack
								if (entity.isInstanceOf[Food]) {
									entityPlayer.hitPoints += 1
								}
							}
							broadcastPlayer(player)
						}
					}
				}
				case _ => Logger warn s"Non-player entity tried to attack a non-living entity"
			}
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
			find(player) map { worldEntity =>
				val pos = worldEntity.pos
				if (pos.distanceTo(target) > Chunk.length) {
					// players can only place blocks nearby
					false
				} else {
					player.selected map { itemIndex =>
						if (itemIndex >= 0 && itemIndex < player.inventory.items.length) {
							val targetTile = tileAt(target)
							targetTile.entity map {_ => true} getOrElse {
								player getSelectedItem() map { stack =>
									val placed = stack.item match {
										case entity:Entity => {
											// FIXME: if the player has a stack, and places multiple,
											// this places the same entity object each time,
											// hence the entity cache bugs. this needs to copy the entity
											// and then place the copy.
											spawnEntity(entity, target)
											true
										}
										case terrain:Terrain => {
											if (!(targetTile.terrain.getClass.isInstance(terrain))) {
												targetTile.terrain = stack.item.asInstanceOf[Terrain]
												broadcastTileEvent(target)
												true
											} else false
										}
										case _ => false
									}
									if (placed) {
										// subtract from player's inventory
										val l0 = player.inventory.items.length
										player.inventory.subtractOneOf(stack)
										val l1 = player.inventory.items.length
										if (l1-l0 != 0) player.selected = None
										broadcastPlayer(player)
									}
									placed
								} getOrElse {
									// player has no selected item (redundant)
									false
								}
							}
						} else {
							// selected item index is out of range (how would this occur?)
							false
						}
					} getOrElse {
						// player has no item selected to place
						false
					}
				}
			} getOrElse {
				Logger warn s"$playerName tried to place an item, but is not spawned"
				false
			}
		} getOrElse {
			Logger warn "Nonexistent player tried to place item: $playerName"
			false
		}
	}

	def doSelectItem(playerName:String, inventoryIndex:Int) = {
		val player:Player = players.get(playerName).get
		if (inventoryIndex < 0 || inventoryIndex >= player.inventory.items.size) {
			// invalid index
		} else {
			player.selected = Some(inventoryIndex)
			broadcastPlayer(player)
		}
	}
	
	def doDeselectItem(playerName:String):Unit = {
		players get playerName map { player =>
			player.selected = None
			broadcastPlayer(player)
		}
	}
	
	def doSwapItems(playerName:String, i0:Int, i1:Int):Unit = {
		players get playerName map { player =>
			if (player.swapItems(i0, i1)) {
				broadcastPlayer(player)
			}
		}
	}
	
	def broadcastTileEvent(pos:WorldCoordinates):Unit = {
		val tile:Tile = tileAt(pos)
		val player:Option[Player] = tileAt(pos).entity match {
			case Some(entity:EntityPlayer) => Some(entity.player)
			case _ => None
		}
		val event:WorldEvent = WorldEvent(timeStr, "tile", Some(pos.x), Some(pos.y), Some(tile), player)
		this.eventChannel.push(event)
	}
	
	def broadcastPlayer(player:Player, kind:String = "player"):Unit = {
		val pos = find(player).get.pos
		val tile:Option[Tile] = Option(tileAt(pos))
		val event:WorldEvent = WorldEvent(timeStr, kind, Some(pos.x), Some(pos.y), tile, Some(player))
		this.eventChannel.push(event)
	}
}
