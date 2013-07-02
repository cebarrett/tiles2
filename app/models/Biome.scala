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
			tile.entity = Some(EntityLlama())
		}
	}
}

case object DesertBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainSand
		if (Math.random < 0.02) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainDirt
		if (Math.random < 0.02) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object StoneBiome extends Biome {
	private val stoneNoise = new GridRandom(
		Seq(
			Limestone, Granite, Sandstone, Basalt
		), 10)
	// FIXME: make the ore clump together like MC, currently mining is tedious and takes too long
	private val oreNoise   = new GridRandom(
		Seq(
			Malachite, Malachite, Malachite, Malachite,
			Cassiterite, Cassiterite, Cassiterite, Hematite,
			Hematite, Hematite, Hematite, Hematite,
			Silver, Silver, Gold, Copper
		), 0.10)

	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainBedrock
		
		var chanceOfOre = 0.03;

		tile.entity = Some({
			if (Math.random < chanceOfOre)
				EntityBlock(oreNoise.pick(pos.x, pos.y).get)
			else
				EntityBlock(stoneNoise.pick(pos.x, pos.y).get)
		});
	}
}
