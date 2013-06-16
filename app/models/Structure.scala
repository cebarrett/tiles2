package models

abstract trait Structure {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object StructureSpawn extends Structure {
	def decorate(tile:Tile, pos:WorldCoordinates) = {
		if (pos.x == -1 && pos.y == -1) {
			tile.entity = Some(EntityWorkbench())
		}
	}
}