package models

case class TileCoordinates(val tx:Int, val ty:Int) {
	def toWorldCoordinates(cc:ChunkCoordinates):WorldCoordinates = {
		WorldCoordinates(cc.cx*Chunk.length+tx, cc.cy*Chunk.length+ty)
	}
	def pos(cc:ChunkCoordinates) = toWorldCoordinates(cc)
}
