package models

import scala.collection.mutable

class ChunkGrid extends mutable.HashMap[ChunkCoordinates,Chunk] {

	def getOrGenerate(coords:ChunkCoordinates):Chunk = {
		if (None == get(coords)) {
			put(coords, ChunkGenerator.generate(coords))
		}
		get(coords) head
	}

}