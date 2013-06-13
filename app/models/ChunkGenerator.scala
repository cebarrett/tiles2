package models

import play.api.Logger
import scala.util.Random

object ChunkGenerator {

	private val biomeGen = new GridRandom[Biome](Seq(DesertBiome, ForestBiome, DirtBiome, StoneBiome))

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
		val tile:Tile = Tile(tc.tx, tc.ty, Terrain("dirt"))
		biomeGen.pick(coords.x, coords.y).map({_.decorate(tile, coords)})
		tile
	}

}
