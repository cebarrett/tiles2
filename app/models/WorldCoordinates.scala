package models

import scala.util.Random

case class WorldCoordinates(val x:Int, val y:Int) {
	// require(0 <= x && x < World.length*Chunk.length && 0 <= y && y < World.length*Chunk.length)
	def toChunkCoordinates():ChunkCoordinates = ChunkCoordinates(Chunk.coord(x), Chunk.coord(y))
	def toTileCoordinates():TileCoordinates   = TileCoordinates(Tile.coord(x), Tile.coord(y))
	def inSameChunk(other:WorldCoordinates):Boolean = {
		val (cc1, cc2) = (toChunkCoordinates, other.toChunkCoordinates)
		(cc1.cx == cc2.cx && cc1.cy == cc2.cy)
	}
	/**
	 * Returns the adjacent coordinates in pseudo-random order.
	 * Can return less than 4 coordinates if some are invalid.
	 */
	def getAdjacent():Seq[WorldCoordinates] = {
		Random.shuffle(Seq(
			WorldCoordinates(World.clamp(x+1),y),
			WorldCoordinates(World.clamp(x-1),y),
			WorldCoordinates(x,World.clamp(y+1)),
			WorldCoordinates(x,World.clamp(y-1))
		))
	}
}
