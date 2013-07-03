package models

trait StructureGen {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object StructureSpawn extends StructureGen {
	def decorate(tile:Tile, pos:WorldCoordinates) = {
		if (pos.x == 0 && pos.y == 0) {
			tile.entity = Some(EntityWorkbench(Diamond))
		}
	}
}

case object StructureWizard extends StructureGen {
	def decorate(tile:Tile, pos:WorldCoordinates) = {
		val T = 0
		val P = if (Game.DEV) 0.05 else 0.0011
		if (tile.terrain == TerrainLava && tile.entity.isEmpty && math.abs(pos.x) > T && math.abs(pos.y) > T && Math.random < P) {
			tile.entity = Some(EntityWizard())
		}
	}
}