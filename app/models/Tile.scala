package models

case class Tile (var terrain:Terrain, var entity:Entity) 

object Tile {
	def coord(worldCoord:Int) = worldCoord - Chunk.coord(worldCoord) * Chunk.length
}