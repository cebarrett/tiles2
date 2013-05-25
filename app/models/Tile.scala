package models

class Tile (var terrain:Terrain, var entity:Entity) {

}

object Tile {
	def coord(n:Int) = n - Chunk.coord(n) * Chunk.length
}