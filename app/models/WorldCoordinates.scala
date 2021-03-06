package models

import math._
import scala.util.Random

case class WorldCoordinates(val x:Int, val y:Int) {
	
	override def toString = s"$getClass($x,$y)"
	
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
	
	def distanceTo(other:WorldCoordinates):Double = {
		sqrt(pow(other.x-x, 2) + pow(other.y-y, 2))
	}
	
	def randomCoordsInRadius(radius:Int):WorldCoordinates = {
		val (minX, maxX) = (x-radius, x+radius)
		val (minY, maxY) = (y-radius, y+radius)
		val randX = (math.random * (maxX-minX+1) + minX).toInt
		val randY = (math.random * (maxY-minY+1) + minY).toInt
		WorldCoordinates(randX, randY)
	}
}

