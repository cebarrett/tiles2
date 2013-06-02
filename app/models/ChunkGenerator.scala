package models

object ChunkGenerator {

	def generate(coords:ChunkCoordinates):Chunk = {
		val chunk = new Chunk(coords.cx, coords.cy)
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {
				val terrain = new Terrain("dirt")
				val entity = {
					if (Math.random < 0.33)
						Some(new EntityTree("tree"))
					else if (Math.random < 0.01)
						Some(new EntityWorkbench("workbench"))
					else None
				}
				chunk.tiles(tx)(ty) = new Tile(tx, ty, terrain, entity)
			}
		}
		chunk;
	}

}
