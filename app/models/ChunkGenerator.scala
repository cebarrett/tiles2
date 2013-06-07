package models

import play.api.Logger

object ChunkGenerator {

	// FIXME: seed the RNG per world

	def generate(coords:ChunkCoordinates):Chunk = {
		val chunk = new Chunk(coords.cx, coords.cy)
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {

				val worldPos:WorldCoordinates = WorldCoordinates(coords.cx*Chunk.length+tx, coords.cy*Chunk.length+ty)
				val noise:Float = calcWorldGenNoise(worldPos)
				val stoneNoise:Float = calcStoneNoise(worldPos)
				val treeNoise:Float = calcTreeNoise(worldPos)

				// FIXME: use/write a weighted list helper class with a method to pick random, it will be needed a lot.
				var entity:Option[Entity] = None
				val terrain = {
					if (noise < -0.25) {
						new Terrain("sand")
					} else if (noise > 0.5 && Math.random < 0.1) {
						entity = Some(EntityOre())
						new Terrain("rock")
					} else if (noise > 0.25) {
						val stone:Stone = {
							if (stoneNoise < -0.5) Stone.BASALT
							else if (stoneNoise < -0.2) Stone.ORTHOCLASE
							else if (stoneNoise < 0.5) Stone.GRANITE
							else Stone.TALC
						}
						entity = Some(EntityStone(stone))
						new Terrain("rock")
					} else {
						entity = {
							if (Math.random < 0.02)
								Some(new EntityTree("tree"))
							else if (Math.random < 0.0005)
								Some(new EntityWorkbench("workbench"))
							else if (Math.random() < 0.005)
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

	private def calcWorldGenNoise(pos:WorldCoordinates):Float = perlinNoise(pos, 0)
	private def calcStoneNoise(pos:WorldCoordinates):Float    = perlinNoise(pos, 25)
	private def calcTreeNoise(pos:WorldCoordinates):Float     = perlinNoise(pos, 50)


	private def perlinNoise(pos:WorldCoordinates, z:Int = 0, scale:Float = 0.1.toFloat):Float = {
		perlinNoise(pos.x, pos.y, z, scale)
	}

	private def perlinNoise(x:Int, y:Int, z:Int, scale:Float):Float = {
		PerlinNoise.perlinNoise((x*scale).toFloat, (y*scale).toFloat, z)
	}

}
