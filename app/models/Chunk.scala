package models

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
}

case class ChunkCoordinates(val cx:Int, val cy:Int) {
	require(0 <= cx && cx < World.length && 0 <= cy && cy < World.length)
}

case class ChunkRadius(val coords:ChunkCoordinates, val radius:Int) {

	val minX:Int = coords.cx - radius
	val maxX:Int = coords.cx + radius
	val minY:Int = coords.cy - radius
	val maxY:Int = coords.cy + radius

	def difference(other:ChunkRadius):Seq[ChunkCoordinates] = {
		var chunkCoords:Seq[ChunkCoordinates] = Seq.empty
		var x:Int = 0
		var y:Int = 0
		for (x <- minX to maxX) {
			for (y <- minY to maxY) {
				var cc:ChunkCoordinates = ChunkCoordinates(x,y)
				if (false == other.contains(cc)) {
					chunkCoords = chunkCoords ++ Seq(cc);
				}
			}
		}
		return chunkCoords
	}
	def contains(coords:ChunkCoordinates):Boolean = {
		(coords.cx >= minX && coords.cx <= maxX && coords.cy >= minY && coords.cy <= maxY);
	}
	def allChunks():Seq[ChunkCoordinates] = {
		var chunkCoords:Seq[ChunkCoordinates] = Seq.empty
		var x:Int = 0
		var y:Int = 0
		for (x <- minX to maxX) {
			for (y <- minY to maxY) {
				var cc:ChunkCoordinates = ChunkCoordinates(x,y)
				chunkCoords = chunkCoords ++ Seq(cc);
			}
		}
		return chunkCoords
	}
}