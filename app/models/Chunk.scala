package models


class Chunk (val cx:Int, val cy:Int, val tiles:Array[Array[Tile]] = Array.ofDim[Tile](Chunk.length, Chunk.length)) {

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