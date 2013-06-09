package models

import scala.util.control.Breaks._

sealed abstract trait AI {
	def tick(world:World, coords:WorldCoordinates):Unit
}

class AIAnimal() extends AI {

	var chanceOfEntityMoving:Double = 0.1;

	def tick(world:World, coords:WorldCoordinates):Unit = {
		breakable {
			if (Math.random() < chanceOfEntityMoving) {
				chanceOfEntityMoving = 0.8
				coords getAdjacent() foreach { c2 =>
					val t2:Tile = world.tile(c2)
					if (t2.entity.isEmpty) {
						world.moveEntity(coords, c2)
						break
					}
				}
			}
		}
	}
}

