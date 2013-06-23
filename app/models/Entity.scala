package models

import scala.util.control.Breaks._
import scala.util.Random

abstract class Entity extends Item {
	def tick(world:World, coords:WorldCoordinates):Unit = {
		// no-op by default
	}
}

trait EntityLiving extends Entity {
	var hitPoints:Int = 1
	def dead:Boolean = (hitPoints <= 0)
	def damage:Unit = (hitPoints = hitPoints-1)
	/**
	 * Attack another entity.
	 * This method is responsible for subtracting hit points and
	 * applying any other effects to the target, but should not
	 * despawn the target if its hit points drop to 0.
	 */
	def attack(target:EntityLiving):Boolean = {
		target.damage
		true
	}
	def drop:Seq[ItemStack] = Seq.empty
}

case class EntityPlayer(val player:Player) extends EntityLiving {
	hitPoints = 10
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
	hitPoints = 20
	def ai:AI = new AIMonster
}

case class EntityLlama() extends EntityAnimal

case class EntityGoblin() extends EntityMonster

case class EntitySapling() extends Entity {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		val tile:Tile = world.tileAt(coords)
		val chanceOfTreeGrowing:Double = 0.0005;
		if (Math.random() < chanceOfTreeGrowing) {
			tile.entity = Some(EntityTree())
			// FIXME: next line seems out of place
			world.broadcastTileEvent(coords)
		}
	}
}

case class EntityTree() extends Entity
case class EntityBlock[T](override val material:T) extends Entity

case class EntityWorkbench() extends Entity
case class EntityKiln() extends Entity
case class EntitySmelter() extends Entity
case class EntitySawmill() extends Entity
case class EntityStonecutter() extends Entity
case class EntityAnvil() extends Entity
