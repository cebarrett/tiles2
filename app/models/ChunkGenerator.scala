package models

object ChunkGenerator {

	def generate(coords:ChunkCoordinates):Chunk = {
		val chunk = new Chunk(coords.cx, coords.cy)
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {
				val terrain = new Terrain("dirt")
				// FIXME: use/write a weighted list helper class with a method to pick random, it will be needed a lot.
				val entity = {
					if (Math.random < 0.03)
						Some(new EntityTree("tree"))
					else if (Math.random < 0.005)
						Some(new EntityWorkbench("workbench"))
					else if (Math.random() < 0.01)
						Some(new EntityLlama())
					else
						None
				}
				chunk.tiles(tx)(ty) = new Tile(tx, ty, terrain, entity)
			}
		}
		chunk;
	}

}
