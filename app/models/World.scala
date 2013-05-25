package models

import scala.collection.mutable._

class World {

	private val chunks  = Array.ofDim[Chunk](World.length, World.length)
	private val players = new HashSet[Player]

	def chunk(x:Int, y:Int, newChunk:Chunk = null):Chunk = {
		require(0 <= x && x < World.length && 0 <= y && y < World.length)
		var chunk = newChunk
		if (chunk != null) {
			chunks(Chunk.coord(x))(Chunk.coord(y)) = chunk;
		} else {
			chunk = chunks(Chunk.coord(x))(Chunk.coord(y));
		}
		/* chunks are lazy created */
		if (chunk == null) {
			chunks(Chunk.coord(x))(Chunk.coord(y)) = ChunkGenerator.generate(Chunk.coord(x),Chunk.coord(y))
			chunk = ChunkGenerator.generate(Chunk.coord(x),Chunk.coord(y))
		}
		chunk
	}

	def tile(x:Int, y:Int, newTile:Tile = null):Tile = {
		require(0 <= x && x < Chunk.length && 0 <= y && y < Chunk.length)
		var tile = newTile
		if (tile != null) {
			chunk(x,y).tile(x,y,tile)
		} else {
			tile = chunk(x,y).tile(x,y)
		}
		tile
	}

}

object World {
	def length:Int = 3;
}