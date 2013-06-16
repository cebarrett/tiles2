package models

import play.api.Logger

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
		tile.terrain = Terrain("sand")
		if (Math.random < 0.01) {
			tile.entity = Some(EntityGoblin())
		}
	}
}

case object DirtBiome extends Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("dirt")
		if (Math.random < 0.02) {
			tile.entity = Some(EntityLlama())
		}
	}
}

case object StoneBiome extends Biome {
	private val stoneNoise = new GridRandom(
		Seq(
			Stone.LIMESTONE, Stone.GRANITE
		), 1.0)
	private val oreNoise   = new GridRandom(
		Seq(
			Metal.COPPER, Metal.COPPER, Metal.COPPER, Metal.COPPER,
			Metal.COPPER, Metal.COPPER, Metal.IRON, Metal.IRON
		), 10.0)

	def decorate(tile:Tile, pos:WorldCoordinates):Unit = {
		tile.terrain = Terrain("bedrock")
		tile.entity = Some({
			if (Math.random < 0.02)
				EntityOre(oreNoise.pick(pos.x, pos.y).get)
			else
				EntityStone(stoneNoise.pick(pos.x, pos.y).get)
		});
	}
}
