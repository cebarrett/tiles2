package models



case class ChunkCoordinates(val cx:Int, val cy:Int) {
	
	def toWorldCoordinates():WorldCoordinates = WorldCoordinates(cx*Chunk.length, cy*Chunk.length)
	
}
