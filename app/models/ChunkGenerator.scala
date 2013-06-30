package models

import play.api.Logger
import scala.util.Random

object ChunkGenerator {
	
	private var terrainGen = new GridNoise(3)

	private val biomeGen = new GridRandom[Biome](
		Seq(ForestBiome, ForestBiome, DesertBiome, DirtBiome),
		1.0)

	private val structureGenList:Seq[StructureGen] = Seq(StructureSpawn)

	def generate(coords:ChunkCoordinates):Chunk = {
		val chunk = new Chunk(coords.cx, coords.cy)
		for (tx <- 0 until Chunk.length) {
			for (ty <- 0 until Chunk.length) {
				val worldPos:WorldCoordinates = WorldCoordinates(coords.cx*Chunk.length+tx, coords.cy*Chunk.length+ty)
				chunk.tiles(tx)(ty) = generate(worldPos)
			}
		}
		chunk
	}

	private def generate(coords:WorldCoordinates):Tile = {
		val tc:TileCoordinates = coords.toTileCoordinates()
		val tile:Tile = Tile(tc.tx, tc.ty, TerrainDirt)
		
		val terrainNoise = terrainGen noiseAt (coords.x, coords.y)
//		Logger debug s"$terrainNoise"
		if (terrainNoise < 0.00) {
			tile.terrain = TerrainWater
		} else if (terrainNoise < 0.01) {
			tile.terrain = TerrainSand
		} else if (terrainNoise < 0.33) {
			biomeGen pick (coords.x, coords.y) map {_ decorate (tile, coords)}
			structureGenList map {_ decorate (tile, coords)}
		} else if (terrainNoise < 0.90) {
			StoneBiome decorate (tile, coords)
		} else if (terrainNoise < 0.91) {
			tile.terrain = TerrainBedrock
			tile.entity = Some(EntityBlock(Obsidian))
		} else {
			tile.terrain = TerrainLava
		}
		
		tile
	}

}
