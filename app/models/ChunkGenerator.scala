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
				val oreNoise:Float = calcOreNoise(worldPos)

				// FIXME: stone and ore generation clump around each other using just 1 perlin noise,
				// instead try writing a class that uses N perlin noise to select random from a list of N^2
				// FIXME: use/write a weighted list helper class with a method to pick random, it will be needed a lot.
				var entity:Option[Entity] = None
				val terrain = {
					if (noise < -0.2) {
						new Terrain("sand")
					} else if (noise > 0.25 && Math.random < 0.02) {
						val ore:Metal = genOre(oreNoise)
						entity = Some(EntityOre(ore))
						new Terrain("bedrock")
					} else if (noise > 0.25) {
						val stone:Stone = genStone(noise)
						entity = Some(EntityStone(stone))
						new Terrain("bedrock")
					} else {
						if (treeNoise > 0) {
							if (Math.random < 0.2) {
								entity = Some(EntityTree())
							}
							Terrain("grass")
						} else {
							if (Math.random < 0.003) {
								entity = Some(EntityLlama())
							}
							Terrain("dirt")
						}
					}
				}

				chunk.tiles(tx)(ty) = new Tile(tx, ty, terrain, entity)
			}
		}
		chunk;
	}

	private def genStone(stoneNoise:Float):Stone = {
		val th1 = 0.4
		if (stoneNoise < -th1) Stone.CHALK
		else if (stoneNoise < 0) Stone.LIMESTONE
		else if (stoneNoise < th1) Stone.GRANITE
		else Stone.BASALT
	}

	private def genOre(oreNoise:Float):Metal = {
		val (th1, th2, th3) = (-0.4, -0.25, 0)
		if      (oreNoise < th1) Metal.GOLD
		else if (oreNoise < th2) Metal.SILVER
		else if (oreNoise < th3) Metal.IRON
		else Metal.COPPER
	}

	private def defaultScale = if (Game.DEV) 0.1f else 0.01f;

	private def calcWorldGenNoise(pos:WorldCoordinates):Float = perlinNoise(pos, 0,   defaultScale)
	private def calcStoneNoise(pos:WorldCoordinates):Float    = perlinNoise(pos, 100, defaultScale / 3)
	private def calcTreeNoise(pos:WorldCoordinates):Float     = perlinNoise(pos, 200, defaultScale * 3)
	private def calcOreNoise(pos:WorldCoordinates):Float      = perlinNoise(pos, 300, defaultScale * 3)

	private def perlinNoise(pos:WorldCoordinates, z:Int = 0, scale:Float):Float = {
		perlinNoise(pos.x, pos.y, z, scale)
	}

	private def perlinNoise(x:Int, y:Int, z:Int, scale:Float):Float = {
		PerlinNoise.perlinNoise((x*scale).toFloat, (y*scale).toFloat, z)
	}

}
