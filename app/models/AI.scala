package models

import scala.util.control.Breaks._

sealed abstract trait AI {
	def tick(world:World, coords:WorldCoordinates, tile:Tile):Unit
}

case object AIAnimal extends AI {
	def tick(world:World, coords:WorldCoordinates, tile:Tile):Unit = {
		val chanceOfEntityMoving:Double = 0.1;
		if (Math.random() < chanceOfEntityMoving) {
			breakable {
				coords getAdjacent() foreach { c2:WorldCoordinates =>
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
