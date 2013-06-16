package models

abstract trait Structure {
	def decorate(tile:Tile, pos:WorldCoordinates):Unit
}

case object StructureSpawn extends Structure {
	def decorate(tile:Tile, pos:WorldCoordinates) = {
		if (pos.x == 5 && pos.y == 5) {
			tile.entity = Some(EntityWorkbench())
		}
	}
}