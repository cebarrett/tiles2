package models

abstract trait Terrain {
	def passable:Boolean = true
	def id:String = this.getClass().getSimpleName().replaceAll("^Terrain|\\$+$", "").toLowerCase()
}

abstract class AbstractTerrain(override val passable:Boolean = true) extends Terrain

object TerrainBedrock extends Terrain
object TerrainDirt extends Terrain
object TerrainGrass extends Terrain
object TerrainSand extends Terrain
object TerrainWater extends AbstractTerrain(passable = false)
object TerrainLava extends AbstractTerrain(passable = false)
