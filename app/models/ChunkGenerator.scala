package models

object ChunkGenerator {

	def generate(x:Int, y:Int):Chunk = {
		val chunk = new Chunk(Chunk.coord(x), Chunk.coord(y))
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {
				val terrain = new Terrain("dirt")
				val entity = if (Math.random < 0.05) new Entity("tree") else null
				chunk.tiles(tx)(ty) = new Tile(terrain, entity)
			}
		}
		chunk;
	}

}
