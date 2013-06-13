package models

abstract trait Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object ForestBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("grass")
		if (Math.random < 0.01) {
			tile.entity = Some(EntityTree())
		}
	}
}

case object DesertBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("desert")
		if (Math.random < 0.01) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("dirt")
		if (Math.random < 0.01) {
			tile.entity = Some(EntityLlama())
		}
	}
}

case object StoneBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("bedrock")
		if (Math.random < 0.02) {
			tile.entity = Some(EntityOre(genOre(fakeNoise())))
		} else {
			tile.entity = Some(EntityStone(genStone(fakeNoise())))
		}
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

	private def fakeNoise():Float = (Math.random * 2 - 1).toFloat
}
