package models

object ChunkGenerator {

	def generate(x:Int, y:Int) {
		val chunk = new Chunk(Chunk.coord(x), Chunk.coord(y))
		for (i <- 0 until Chunk.length) {
			for (j <- 0 until Chunk.length) {
				
			}
		}
		chunk;
	}

}