package models

import scala.collection.mutable._

class World {

	private val chunks  = Array.ofDim[Chunk](World.length, World.length)
	private val players = new HashSet[Player]

	def chunk(x:Int, y:Int, newChunk:Chunk = null):Chunk = {
		require(0 <= x && x < World.length && 0 <= y && y < World.length)
		var c = newChunk
		if (c != null) {
			chunks(Chunk.coord(x))(Chunk.coord(y)) = c;
		} else {
			c = chunks(Chunk.coord(x))(Chunk.coord(y));
		}
		/* chunks are lazy created */
		if (c == null) {
			chunks(Chunk.coord(x))(Chunk.coord(y)) = ChunkGenerator.generate(Chunk.coord(x),Chunk.coord(y))
			c = chunk(x,y)
		}
		c
	}

	def tile(x:Int, y:Int, newTile:Tile = null):Tile = {
		require(0 <= x && x < Chunk.length && 0 <= y && y < Chunk.length)
		var t = newTile
		if (t != null) {
			chunk(x,y).tile(x,y,t)
		} else {
			t = chunk(x,y).tile(x,y)
		}
		t
	}

}

object World {
	def length:Int = 3;
}