package models

import play.api.Logger

abstract trait Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object ForestBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainGrass
		if (Math.random < 0.125) {
			tile.entity = Some(new EntityTree())
		} else if (Math.random < 0.005) {
			tile.entity = Some(new EntityPig())
		}
	}
}

case object DesertBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainSand
		if (Math.random < 0.03) {
			tile.entity = Some(new EntitySpider())
		}
	}
}

case object SnowBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainSnow
		if (Math.random < 0.01) {
			tile.entity = Some(new EntityTree())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainDirt
		if (Math.random < 0.01) {
			tile.entity = Some(new EntityTree())
		}
		if (Math.random < 0.01) {
			tile.entity = Some(new EntityGoblin())
		}
	}
}

case object StoneBiome extends Biome {
	private val stoneNoise = new GridRandom(Seq(
		Sandstone, Diorite, Mudstone, Gabbro,
		Limestone, Basalt, Shale, Marble,
		Slate, Granite
	), 4)
	private val oreNoise = new GridRandom(Seq(
		Cassiterite, Hematite, Malachite, Ilmenite,
		Malachite, Silver, Sphalerite, Germanium,
		Cassiterite, Hematite, Galena, Germanium,
		Silver, Platinum, Copper, Gold,
		Hematite, Galena, Cassiterite, Cassiterite,
		Malachite, Ilmenite, Malachite, Electrum,
		Hematite, Malachite, Hematite, Ilmenite,
		Sphalerite, Sphalerite, Hematite, Galena
	), 0.2)
	private val oreGenNoise = new GridNoise(0.20)

	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainBedrock
		val oreThreshold = 0.86 // between 0 and 1
		tile.entity = Some({
			if (Math.abs(oreGenNoise.noiseAt(pos.x, pos.y)) > oreThreshold)
				new EntityBlock(oreNoise.pick(pos.x, pos.y).get)
			else
				new EntityBlock(stoneNoise.pick(pos.x, pos.y).get)
		})
	}
}
