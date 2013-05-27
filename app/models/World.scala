package models

class World {

	private val chunks  = Array.ofDim[Option[Chunk]](World.length, World.length)

	def chunk(cx:Int, cy:Int):Chunk = chunk(ChunkCoordinates(cx,cy))

	def chunk(coords:ChunkCoordinates):Chunk = {
		val (cx, cy) = (coords.cx, coords.cy)
		/* chunks are lazy created */
		if (chunks(cx)(cy) == null) {
			chunks(cx)(cy) = Option.apply[Chunk](ChunkGenerator.generate(cx, cy))
		}

		return (chunks(cx)(cy) getOrElse null)
	}

	def tile(x:Int, y:Int):Tile = {
		return chunk(WorldCoordinates(x, y).toChunk()).tile(x,y)
	}

}

case class WorldCoordinates(val x:Int, val y:Int) {
	require(0 <= x && x < World.length*Chunk.length && 0 <= y && y < World.length*Chunk.length)
	def toChunk():ChunkCoordinates = ChunkCoordinates(Math.floor(x / Chunk.length).toInt, Math.floor(y / Chunk.length).toInt)
}

object World {
	def length:Int = 3;	// length in chunks
}