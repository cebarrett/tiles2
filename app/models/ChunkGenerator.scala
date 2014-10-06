package models

import play.api.Logger
import scala.util.Random

object ChunkGenerator {
	
	private var terrainGen = new GridNoise(1.0)

	private val biomeGen = new GridRandom[Biome](
		Seq(ForestBiome, SnowBiome, DesertBiome, DirtBiome),
		2)

	private val structureGen:Seq[StructureGen] = Seq(StructureSpawn, StructureBoss)

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
		// grab the tile to generate
		val tc:TileCoordinates = coords.toTileCoordinates()
		val tile:Tile = Tile(tc.tx, tc.ty, TerrainDirt)
		
		// use terrain noise to pick initial terrain
		// if flat land, then decorate with a biome
		val terrainNoise = terrainGen noiseAt (coords.x, coords.y)
		if (terrainNoise < 0.00) {
			tile.terrain = TerrainWater
		} else if (terrainNoise < 0.01) {
			tile.terrain = TerrainSand
		} else if (terrainNoise < 0.33) {
			biomeGen pick (coords.x, coords.y) map {_ decorate (tile, coords)}
		} else if (terrainNoise < 0.91) {
			StoneBiome decorate (tile, coords)
		} else if (terrainNoise < 0.92) {
			tile.terrain = TerrainBedrock
			tile.entity = Some(new EntityBlock(Obsidian))
		} else {
			tile.terrain = TerrainLava
		}
		
		// generate structures
		structureGen map {_ decorate (tile, coords)}
		
		// done
		return tile
	}
}
