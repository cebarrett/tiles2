package models

object World {
	def length:Int = 2;	// length in chunks
}

class World {

	val chunkGrid = new ChunkGrid

	def chunk(cx:Int, cy:Int):Chunk = chunk(ChunkCoordinates(cx,cy))

	def chunk(coords:ChunkCoordinates):Chunk = chunkGrid.getOrGenerate(coords)

	def tile(x:Int, y:Int):Tile = {
		return chunk(WorldCoordinates(x, y).toChunkCoordinates()).tile(x,y)
	}

}

case class WorldCoordinates(val x:Int, val y:Int) {
	require(0 <= x && x < World.length*Chunk.length && 0 <= y && y < World.length*Chunk.length)
	def toChunkCoordinates():ChunkCoordinates = ChunkCoordinates(Math.floor(x / Chunk.length).toInt, Math.floor(y / Chunk.length).toInt)
}
