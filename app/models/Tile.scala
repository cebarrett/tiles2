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

case class TileCoordinates(val tx:Int, val ty:Int) {
	def toWorldCoordinates(cc:ChunkCoordinates):WorldCoordinates = {
		WorldCoordinates(cc.cx*Chunk.length+tx, cc.cy*Chunk.length+ty)
	}
	def pos(cc:ChunkCoordinates) = toWorldCoordinates(cc)
}
