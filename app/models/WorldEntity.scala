package models

import play.api.{Logger => log}

class WorldEntity[T <: Entity](val entity:T, val world:World) {
	def unapply = (entity, world)
	def pos:WorldCoordinates = {
		world.pos(entity) getOrElse {
			log warn s"WorldEntity not found in world for entity $entity"
			null
		}
	}
}
