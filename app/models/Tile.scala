package models

case class Tile (val tx:Int, val ty:Int, var terrain:Terrain, var entity:Option[Entity] = None) {
	def coords = TileCoordinates(tx, ty)
	def removeItem:Option[Entity] = {val e=entity ; entity=None ; e}
}

object Tile {
	def coord(worldCoord:Int) = {
		(((worldCoord % Chunk.length) + Chunk.length) % Chunk.length)
	}
}
