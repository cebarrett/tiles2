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
							val th1 = 0.4
							if (stoneNoise < -th1) Stone.CHALK
							else if (stoneNoise < 0) Stone.LIMESTONE
							else if (stoneNoise < th1) Stone.GRANITE
							else Stone.BASALT
						}
						entity = Some(EntityStone(stone))
						new Terrain("rock")
					} else {
						entity = {
							if (treeNoise > 0.25 && Math.random < 0.33)
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

	private def defaultScale:Float = 0.3.toFloat;
	private def calcWorldGenNoise(pos:WorldCoordinates):Float = perlinNoise(pos, 0,  defaultScale)
	private def calcStoneNoise(pos:WorldCoordinates):Float    = perlinNoise(pos, 25, defaultScale / 3)
	private def calcTreeNoise(pos:WorldCoordinates):Float     = perlinNoise(pos, 50, defaultScale * 3)

	private def perlinNoise(pos:WorldCoordinates, z:Int = 0, scale:Float):Float = {
		perlinNoise(pos.x, pos.y, z, scale)
	}

	private def perlinNoise(x:Int, y:Int, z:Int, scale:Float):Float = {
		PerlinNoise.perlinNoise((x*scale).toFloat, (y*scale).toFloat, z)
	}

}
