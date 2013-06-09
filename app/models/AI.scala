package models

import scala.util.control.Breaks._

sealed abstract class AI {
	def tick(world:World, coords:WorldCoordinates):Unit = Unit
}

/**
 * AI that slowly wanders around.
 */
class AIAnimal() extends AI {
	var chanceOfEntityMoving:Double = 0.05;
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		breakable {
			if (Math.random() < chanceOfEntityMoving) {
				chanceOfEntityMoving = 0.8
				coords getAdjacent() foreach { c2 =>
					val t2:Tile = world.tileAt(c2)
					if (t2.entity.isEmpty) {
						world.moveEntity(coords, c2)
						break
					}
				}
			}
		}
	}
}

class AIMonster() extends AI {}