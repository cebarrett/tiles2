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

/**
 * AI that stands still but attacks adjacent entities.
 */
class AIWall extends AI {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		coords getAdjacent() foreach { adjPos =>
			world.tileAt(adjPos).entity.map({ adjEntity =>
				adjEntity match {
					case _:EntityPlayer => world.doEntityInteraction(coords, adjPos)
					case _ => Unit // only attack players for now, there's a bug
				}
				
			})
		}
	}
}

class AIMonster extends AIWall {

}
