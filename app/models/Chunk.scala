package models

import scala.collection._
import play.api.Logger

case class Chunk (val cx:Int, val cy:Int, val tiles:Array[Array[Tile]] = Array.ofDim[Tile](Chunk.length, Chunk.length)) {

	def tile(x:Int, y:Int, tile:Tile = null):Tile = {
		if (tile != null) {
			tiles(Tile.coord(x))(Tile.coord(y)) = tile
		}
		tiles(Tile.coord(x))(Tile.coord(y))
	}

}

object Chunk {
	val length:Int = 16
	def coord(worldCoord:Int):Int = math.floor(worldCoord/length).toInt
	def clamp(n:Int):Int = Math.min(Math.max(0, n), World.lengthChunks-1);
	def radius(coords:ChunkCoordinates, radius:Int):Set[ChunkCoordinates] = {
		val minX:Int = Chunk.clamp(coords.cx - radius)
		val maxX:Int = Chunk.clamp(coords.cx + radius)
		val minY:Int = Chunk.clamp(coords.cy - radius)
		val maxY:Int = Chunk.clamp(coords.cy + radius)
		var chunksInRadius:Set[ChunkCoordinates] = Set.empty
		for (cx <- minX to maxX) {
			for (cy <- minY to maxY) {
				chunksInRadius = chunksInRadius + ChunkCoordinates(cx, cy)
			}
		}
		return chunksInRadius
	}
}

case class ChunkCoordinates(val cx:Int, val cy:Int) {
	require(0 <= cx && cx < World.length && 0 <= cy && cy < World.length)
}
