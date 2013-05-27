package models

case class Tile (val tx:Int, val ty:Int, var terrain:Terrain, var entity:Entity)

object Tile {
	def coord(worldCoord:Int) = worldCoord - Chunk.coord(worldCoord) * Chunk.length
}