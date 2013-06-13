package models

case class Tile (val tx:Int, val ty:Int, var terrain:Terrain, var entity:Option[Entity] = None)

object Tile {
	def coord(worldCoord:Int) = worldCoord - Chunk.coord(worldCoord) * Chunk.length
}

case class TileCoordinates(val tx:Int, val ty:Int) {
	require(0 <= tx && tx < Chunk.length && 0 <= ty && ty < Chunk.length)
	def toWorldCoordinates(cc:ChunkCoordinates):WorldCoordinates = {
		WorldCoordinates(cc.cx*Chunk.length+tx, cc.cy*Chunk.length+ty)
	}
}
