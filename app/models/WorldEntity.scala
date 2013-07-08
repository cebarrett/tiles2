package models

case class WorldEntity(val entity:Entity, val world:World) {
	def pos:WorldCoordinates = null	// TODO
}