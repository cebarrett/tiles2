package models

import scala.collection._
import scala.util.control.Breaks._
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

	/** Emits WorldEvent to notify listeners when things happen in the world. */
	val (eventEnumerator, eventChannel) = Concurrent.broadcast[WorldEvent]

	/** List of all players in the world, keyed by name */
	var players = Map.empty[String,Player]

	def chunk(cx:Int, cy:Int):Chunk = chunk(ChunkCoordinates(cx,cy))

	def chunk(coords:ChunkCoordinates):Chunk = chunkGrid.getOrGenerate(coords)

	def tile(x:Int, y:Int):Tile = {
		return chunk(WorldCoordinates(x, y).toChunkCoordinates()).tile(x,y)
	}

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
		val playerEntity = (theTile.entity = Some(new PlayerEntity(playerName)))
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
			if (newTile.entity.isDefined) return
			player.x = player.x + dx
			player.y = player.y + dy
			newTile.entity = oldTile.entity
			oldTile.entity = None
			this.eventChannel.push(WorldEvent("playerMoveOldTile", Some(oldX), Some(oldY), Some(oldTile)))
			this.eventChannel.push(WorldEvent("playerMoveNewTile", Some(newX), Some(newY), Some(newTile), Some(player)))
		}
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
	val player:Option[Player] = None
)