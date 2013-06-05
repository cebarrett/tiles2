package models

import play.api.Logger

object ChunkGenerator {

	def generate(coords:ChunkCoordinates):Chunk = {
		val chunk = new Chunk(coords.cx, coords.cy)
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {

				val worldPos = WorldCoordinates(coords.cx*Chunk.length+tx, coords.cy*Chunk.length+ty)
				val scale:Float = (0.025).toFloat // smaller = zoom out
				val noise:Float = PerlinNoise.perlinNoise((worldPos.x*scale).toFloat, (worldPos.y*scale).toFloat)

				// FIXME: use/write a weighted list helper class with a method to pick random, it will be needed a lot.
				var entity:Option[Entity] = None
				val terrain = {
					if (noise < 0) {
						new Terrain("sand")
					} else {
						entity = {
							if (Math.random < 0.03)
								Some(new EntityTree("tree"))
							else if (Math.random < 0.005)
								Some(new EntityWorkbench("workbench"))
							else if (Math.random() < 0.01)
								Some(new EntityLlama())
							else
								None
						}
						new Terrain("dirt")
					}
				}

				chunk.tiles(tx)(ty) = new Tile(tx, ty, terrain, entity)
			}
		}
		chunk;
	}

}
