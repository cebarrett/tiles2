package models

abstract class Terrain(val passable:Boolean = true) {
	def id:String = this.getClass().getSimpleName().replaceAll("^Terrain|\\$+$", "").toLowerCase()
}

object TerrainBedrock extends Terrain
object TerrainDirt extends Terrain
object TerrainGrass extends Terrain
object TerrainSand extends Terrain
object TerrainWater extends Terrain(passable = false)
object TerrainLava extends Terrain(passable = false)
