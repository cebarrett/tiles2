package models

sealed abstract trait Biome {
	def decorate(tile:Tile, pos:WorldCoordinates):Tile
}
