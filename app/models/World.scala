package models

class World {

	private val chunks  = Array.ofDim[Chunk](World.length, World.length)
	val players = Set.empty[String]

	def chunk(x:Int, y:Int):Chunk = {
		val coords = WorldCoordinates(x, y)
		val cx = Chunk.coord(x)
		val cy = Chunk.coord(y)
		var c = chunks(cx)(cy);

		/* chunks are lazy created */
		if (c == null) {
			chunks(cx)(cy) = ChunkGenerator.generate(cx, cy)
			c = chunk(x,y)
		}

		return c
	}

	def tile(x:Int, y:Int):Tile = {
		WorldCoordinates(x, y)
		return chunk(x,y).tile(x,y)
	}

}

case class WorldCoordinates(val x:Int, val y:Int) {
	require(0 <= x && x < World.length*Chunk.length && 0 <= y && y < World.length*Chunk.length)
}

object World {
	def length:Int = 1;	// length in chunks
}