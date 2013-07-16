package models

import play.api.{Logger => log}

class WorldEntity(val entity:Entity, val world:World) {
	def unapply = (entity, world)
	def pos:WorldCoordinates = {
		world.pos(entity) getOrElse {
			log error s"WorldEntity not found in world for entity $entity"
			throw new RuntimeException
		}
	}
}
