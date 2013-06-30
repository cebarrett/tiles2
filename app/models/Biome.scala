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
		} else if (Math.random < 0.005 && !Game.DEV) {
			tile.entity = Some(EntityLlama())
		}
	}
}

case object DesertBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainSand
		if (Math.random < 0.02 && !Game.DEV) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainDirt
		if (Math.random < 0.02 && !Game.DEV) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object StoneBiome extends Biome {
	private val stoneNoise = new GridRandom(
		Seq(
			Limestone, Granite, Sandstone, Basalt
		), 10)
	private val oreNoise   = new GridRandom(
		Seq(
			Malachite, Malachite, Malachite, Cassiterite,
			Cassiterite, Cassiterite, Hematite, Hematite,
			Hematite, Hematite, Hematite, Copper,
			Silver, Silver, Gold, Cassiterite
		), 0.10)

	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = TerrainBedrock
		
		var chanceOfOre = 0.01;

		tile.entity = Some({
			if (Math.random < 0.02)
				EntityBlock(oreNoise.pick(pos.x, pos.y).get)
			else
				EntityBlock(stoneNoise.pick(pos.x, pos.y).get)
		});
	}
}
