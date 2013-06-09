package models

import scala.util.control.Breaks._
import scala.util.Random

sealed abstract class Entity {
	def id:String
	def tick(world:World, coords:WorldCoordinates):Unit = {
		// no-op by default
	}
}

sealed abstract class EntityLiving extends Entity {
	var hitPoints:Int = 1
	def dead:Boolean = (hitPoints <= 0)
	def damage:Unit = (hitPoints = hitPoints-1)
}

case class EntityPlayer(val player:Player, val id:String = "player") extends EntityLiving {
	/**
	 * Attack another entity.
	 * This method is responsible for subtracting hit points and
	 * applying any other effects to the target, but should not
	 * despawn the target if its hit points drop to 0.
	 */
	hitPoints = 10
	def attack(target:EntityLiving):Boolean = {
		// if (Math.random < 0.5) {
			target.damage
			true
		// } else {
			// false
		// }
	}
}

sealed abstract class EntityMob extends EntityLiving {
	def ai:AI
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		ai.tick(world, coords)
	}
}

sealed abstract class EntityAnimal extends EntityMob {
	def ai:AI = new AIAnimal
}

sealed abstract class EntityMonster extends EntityMob {
	def ai:AI = new AIMonster
}

case class EntityLlama(val id:String = "llama") extends EntityAnimal

case class EntityGoblin(val id:String = "goblin") extends EntityMonster


/*
 * FIXME: too many entities that just correspond 1-1 with an item
 * and don't do anything else. make an EntityItem or an ItemEntity
 */

case class EntitySapling(val id:String = "sapling") extends Entity {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		val tile:Tile = world.tileAt(coords)
		val chanceOfTreeGrowing:Double = 0.0005;
		if (Math.random() < chanceOfTreeGrowing) {
			tile.entity = Some(EntityTree())
			// FIXME: next line seems out of place
			world.eventChannel.push(WorldEvent("entitySpawn", Some(coords.x), Some(coords.y), Some(tile)))
		}
	}
}

case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityStone(val material:Stone, val id:String = "stone") extends Entity
case class EntityOre(val material:Metal, val id:String = "ore") extends Entity

case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityFurnace(val id:String = "furnace") extends Entity
case class EntitySawmill(val id:String = "sawmill") extends Entity
case class EntityStonecutter(val id:String = "stonecutter") extends Entity