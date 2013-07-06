package models

import play.api.Logger

abstract trait Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object ForestBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainGrass
		if (Math.random < 0.125) {
			tile.entity = Some(EntityTree())
		} else if (Math.random < 0.005) {
			tile.entity = Some(EntityPig())
		}
	}
}

case object DesertBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainSand
		if (Math.random < 0.03) {
			tile.entity = Some(EntitySpider())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainDirt
		if (Math.random < 0.01) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object StoneBiome extends Biome {
	private val stoneNoise = new GridRandom(
		Seq(Limestone, Granite, Sandstone, Basalt), 5)
	private val oreNoise = new GridRandom(Seq(
			Malachite, Malachite, Malachite, Malachite,
			Cassiterite, Cassiterite, Cassiterite, Hematite,
			Hematite, Hematite, Hematite, Hematite,
			Silver, Silver, Gold, Copper), 0.09)
	private val oreGenNoise = new GridNoise(0.05)

	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainBedrock
		val oreThreshold = 0.93;
		tile.entity = Some({
			if (oreGenNoise.noiseAt(pos.x, pos.y) > oreThreshold)
				EntityBlock(oreNoise.pick(pos.x, pos.y).get)
			else
				EntityBlock(stoneNoise.pick(pos.x, pos.y).get)
		})
	}
}
