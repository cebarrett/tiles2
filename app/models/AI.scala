package models

import scala.util.control.Breaks._

sealed abstract class AI {
	def tick(world:World, coords:WorldCoordinates):Unit = Unit
}

/**
 * AI that slowly wanders around.
 */
class AIAnimal() extends AI {
	var chanceOfEntityMoving:Double = 0.1;
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		breakable {
			if (Math.random() < chanceOfEntityMoving) {
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

class AIMonster extends AI {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		world.players.values.filter({ player:Player =>
			val playerCoords = WorldCoordinates(player.x, player.y)
			(playerCoords.distanceTo(coords) < Chunk.length)
		}).headOption map { player =>
			val playerCoords = WorldCoordinates(player.x, player.y)
			val dir:Direction = new Path(playerCoords).directionFrom(coords)
			val nextPos = WorldCoordinates(coords.x + dir.x, coords.y + dir.y)
			world.tileAt(nextPos).entity map { entity =>
				if (entity.isInstanceOf[EntityPlayer])
					world.doEntityInteraction(coords, nextPos)
			} getOrElse {
				if (Math.random < 0.75) world.moveEntity(coords, nextPos)
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
