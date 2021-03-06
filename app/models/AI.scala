package models

import scala.util.control.Breaks._

sealed abstract class AI {
	def tick(world:World, coords:WorldCoordinates):Unit = Unit
	def wander(world:World, coords:WorldCoordinates) = {
		breakable {
			if (Math.random() < 0.03) {
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
 * AI that slowly wanders around.
 */
class AIAnimal() extends AI {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		wander(world, coords)
	}
}

class AIMonster extends AI {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		val entity = world.entity(coords)
		if (world.isNight == false && entity.get.isInstanceOf[EntityDragon] == false) {
			// all monsters except dragons start dying off at sunrise
			if (Math.random < 0.02) {
				world.despawnEntity(coords)
				return
			}
		}
		// find a nearby player and attack or move toward
		val aggroRange = Chunk.length
		world.players.filter({ player:Player =>
			world find player map { worldEntity =>
				(worldEntity.pos.distanceTo(coords) < aggroRange)
			} getOrElse false
		}).headOption.map({ player =>
			val playerCoords = world.find(player).get.pos
			val dir:Direction = new Path(playerCoords).directionFrom(coords)
			val nextPos = WorldCoordinates(coords.x + dir.x, coords.y + dir.y)
			world.tileAt(nextPos).entity map { entity =>
				if (entity.isInstanceOf[EntityPlayer])
					world.doEntityInteraction(coords, nextPos)
			} getOrElse {
				// monsters don't pass through doors
				if (world.tileAt(nextPos).terrain.isInstanceOf[Door] == false)
					world.moveEntity(coords, nextPos)
			}
		}).getOrElse({
			wander(world, coords)
		})
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
