package models

import scala.util.control.Breaks._
import scala.util.Random

sealed abstract class Entity {
	def id:String
	def tick(world:World, coords:WorldCoordinates, t:Tile):Unit = {
		// no-op by default
	}
}

sealed abstract class EntityLiving extends Entity

case class EntityPlayer(val playerName:String, val id:String = "player") extends EntityLiving

sealed abstract class EntityMob extends EntityLiving {
	def ai:AI
}

case class EntityLlama(val id:String = "llama", val ai:AI = AI.Animal) extends EntityMob {
	override def tick(world:World, coords:WorldCoordinates, t:Tile):Unit = {
		val chanceofLlamaMoving:Double = 0.1;
		if (Math.random() < chanceofLlamaMoving && this.id == "llama") {
			breakable {
				coords getAdjacent() foreach { c2:WorldCoordinates =>
					val t2:Tile = world.tile(c2)
					val isTerrainPassableByLlamas = (t2.terrain.id != "sand")
					if (isTerrainPassableByLlamas && t2.entity.isEmpty) {
						world.moveEntity(coords, c2)
						break
					}
				}
			}
		}
	}
}

/*
 * FIXME: too many entities that just correspond 1-1 with an item
 * and don't do anything else. make an EntityItem or an ItemEntity
 */

case class EntitySapling(val id:String = "sapling") extends Entity {
	override def tick(world:World, coords:WorldCoordinates, t:Tile):Unit = {
		val chanceOfTreeGrowing:Double = 0.0005;
		if (Math.random() < chanceOfTreeGrowing && this.id == "sapling") {
			t.entity = Some(EntityTree())
			// FIXME: next line seems out of place
			world.eventChannel.push(WorldEvent("entitySpawn", Some(coords.x), Some(coords.y), Some(t)))
		}
	}
}

case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWood(val id:String = "wood") extends Entity  // FIXME: remove
case class EntityStone(val material:Stone, val id:String = "stone") extends Entity
case class EntityOre(val id:String = "ore") extends Entity

case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityFurnace(val id:String = "furnace") extends Entity
case class EntitySawmill(val id:String = "sawmill") extends Entity
case class EntityStonecutter(val id:String = "stonecutter") extends Entity