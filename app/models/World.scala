package models

import scala.collection._
import scala.util.control.Breaks._
import scala.util.Random
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

object World {
	def length:Int = 2;
	def lengthChunks = length
	def lengthTiles = lengthChunks * Chunk.length
}

class World {

	/** Grid of all the chunks in the world */
	val chunkGrid = new ChunkGrid

	/**
	 * Emits WorldEvent to notify listeners when things happen in the world.
	 * Note: World should not send messages to specific players.
	 */
	val (eventEnumerator, eventChannel) = Concurrent.broadcast[WorldEvent]

	/** List of all players in the world, keyed by name */
	var players = Map.empty[String,Player]

	def chunk(cx:Int, cy:Int):Chunk = chunk(ChunkCoordinates(cx,cy))

	def chunk(coords:ChunkCoordinates):Chunk = chunkGrid.getOrGenerate(coords)

	def tile(coords:WorldCoordinates):Tile = tile(coords.x, coords.y)

	def tile(x:Int, y:Int):Tile = {
		return chunk(WorldCoordinates(x, y).toChunkCoordinates()).tile(x,y)
	}

	def entity(coords:WorldCoordinates):Option[Entity] = tile(coords).entity

	def spawnPlayer(playerName:String):Player = {
		// determine a suitable spawn location
		// for now, spawn everyone within 30,30 of the origin
		var theTile:Tile = null
		var (x, y) = (0, 0)
		breakable { for (i <- 0 until 30) {
			x = i
			for (j <- 0 until 30) {
				y = j
				theTile = tile(x, y)
				if (theTile.entity.isEmpty) break
			}
		}}
		// spawn a player entity
		val playerEntity = (theTile.entity = Some(new EntityPlayer(playerName)))
		// broadcast entity spawn
		this.eventChannel.push(WorldEvent("playerSpawn", Some(x), Some(y), Some(theTile)))
		// create a player object and hold a reference
		val player = new Player(playerName, x, y)
		players = players + (playerName -> player)
		return player
	}

	def despawnPlayer(playerName:String) {
		players.get(playerName) match {
			case Some(player) =>
				val (x, y) = (player.x, player.y)
				val theTile = tile(x, y)
				// remove the player entity
				theTile.entity = None
				// broadcast entity despawn
				this.eventChannel.push(WorldEvent("playerDespawn", Some(x), Some(y), Some(theTile)))
				// null out reference to the player object
				players = players - playerName
			case None =>
				// FIXME: there is a bug where this happens if i open around 50
				// browser tabs w/ the game open then close them all at the same time.
				Logger.warn(s"Tried to despawn player who is not logged in: $playerName")
		}
	}

	def movePlayer(playerName:String, dx:Int, dy:Int) {
		players.get(playerName) map { player =>
			val oldX:Int = player.x
			val oldY:Int = player.y
			val (newX, newY) = (oldX+dx, oldY+dy)
			if (newX < 0 || newX >= World.lengthTiles || newY < 0 || newY >= World.lengthTiles) return
			val oldTile = tile(oldX, oldY)
			val newTile = tile(newX, newY)
			(newTile.entity.isEmpty) match {
				case true => {
					// No entity occupying the tile so move there
					player.x = player.x + dx
					player.y = player.y + dy
					newTile.entity = oldTile.entity
					oldTile.entity = None
					this.eventChannel.push(WorldEvent("playerMoveOldTile", Some(oldX), Some(oldY), Some(oldTile)))
					// FIXME: This broadcasts the entire player object, including inventory.
					// We might want to keep a player's inventory secret instead.
					this.eventChannel.push(WorldEvent("playerMoveNewTile", Some(newX), Some(newY), Some(newTile), Some(player)))
				} case false => {
					// An entity is occupying this tile, so interact with it.
					// FIXME: this logic is specific to trees but will happen for players too
					doEntityInteraction(WorldCoordinates(oldX,oldY), WorldCoordinates(newX,newY))
				}
			}
		}
	}
	
	def doPlayerCrafting(playerName:String, recipe:WorkbenchRecipe) {
		val player:Player = players.get(playerName).get
		// Validate that the player has enough items to make the recipe.
		var hasIngredients:Boolean = true
		for (i:Item <- recipe.ingredients) {
			if (player.inventory.has(i) == false) {
				hasIngredients = false
			}
		}
		if (hasIngredients) {
			val playerTile:Tile = tile(player.x, player.y)
			player.inventory.add(recipe.result);
			recipe.ingredients.map {player.inventory.subtract(_)}
			this.eventChannel.push(WorldEvent("playerCraft", Some(player.x), Some(player.y), Some(playerTile), Some(player)))
		} else {
			Logger.warn(s"Player $playerName did not have the ingredients to craft $recipe")
		}
	}

	def doEntityInteraction(attackerCoords:WorldCoordinates, targetCoords:WorldCoordinates) {
		require(entity(attackerCoords).isDefined && entity(targetCoords).isDefined)
		val (attacker:Entity, target:Entity) = (entity(attackerCoords).head, entity(targetCoords).head)
		val (attackerTile:Tile, targetTile:Tile) = (tile(attackerCoords), tile(targetCoords))
		(attacker, target) match {
			case (attacker:EntityPlayer, target:EntityTree) => {
				// if being attacked by a player, drop items
				// very rarely despawn the tree and give player logs
				val player = players.get(attacker.playerName).get
				if (Random.nextDouble() < 0.01) {
					player.inventory.add(Item("apple", Some(1)))
				} else if (Random.nextDouble() < 0.05) {
					player.inventory.add(Item("stick", Some(1+Random.nextInt(2))))
				} else if (Random.nextDouble() < 0.005) {
					player.inventory.add(Item("log", Some(1)))
					targetTile.entity = None
					this.eventChannel.push(WorldEvent("entityDespawn", Some(targetCoords.x), Some(targetCoords.y), Some(targetTile)))
				}
			}
			case (attacker:EntityPlayer, target:EntityPlayer) => {
				val player = players.get(attacker.playerName).get
				if (Random.nextDouble() < 0.1) {
					player.inventory.add(Item("human meat", Some(1)))
				}
			}
			case (attacker:EntityPlayer, target:EntityWorkbench) => {
				val player = players.get(attacker.playerName).get
				val options:Seq[String] = Seq(
					"Close",
					"Craft 4 wood from 1 log",
					"Craft 1 wooden axe from 1 stick and 1 wood"
				)
				this.eventChannel.push(WorldEvent("gui", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player), Some(options)))
			}
			case (_, _) => Unit
		}
		// FIXME: assumes attacker is a player, in the future it can be another kind of mob
		val player = players.get(attacker.asInstanceOf[EntityPlayer].playerName).get
		this.eventChannel.push(WorldEvent("playerUpdate", Some(attackerCoords.x), Some(attackerCoords.y), Some(attackerTile), Some(player)))
	}
}

case class WorldCoordinates(val x:Int, val y:Int) {
	require(0 <= x && x < World.length*Chunk.length && 0 <= y && y < World.length*Chunk.length)
	def toChunkCoordinates():ChunkCoordinates = ChunkCoordinates(Chunk.coord(x), Chunk.coord(y))
	def toTileCoordinates():TileCoordinates   = TileCoordinates(Tile.coord(x), Tile.coord(y))
}

case class WorldEvent(
	val kind:String,
	val x:Option[Int] = None,
	val y:Option[Int] = None,
	val tile:Option[Tile] = None,
	val player:Option[Player] = None,
	val options:Option[Seq[String]] = None
)