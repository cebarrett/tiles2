package models

import scala.util.control.Breaks._
import scala.util.Random

/**
 * An Entity is a kind of item that can also occupy a tile.
 */
trait Entity extends Item {
	/** Runs once per tick when this entity is in a tile in the game world.
	 *  No-op by default but can be overridden. */
	def tick(world:World, coords:WorldCoordinates):Unit = Unit
	// XXX: next two methods are currently for non-living target entities only,
	// need to clean up World.doEntityInteraction to do living
	def canBeBrokenBy(tool:Option[Tool]):Boolean = false
	def drops:Seq[ItemStack] = Seq.empty
}

/**
 * A Block is a kind of entity made out of a material.
 * It can be placed to occupy a tile, and is also used
 * in crafting recipes that require a material ingredient.
 */
class EntityBlock(override val material:Material)
extends AbstractItemWithMaterial(material) with Entity
{
	override def defense = material.hardness
	override def canBeBrokenBy(tool:Option[Tool]):Boolean =
		tool map { _.isInstanceOf[Pick] } getOrElse false
}

class Food() extends Entity {
	override def canBeBrokenBy(o:Option[Tool]) = true
}

abstract class EntityLiving extends Entity {
	var hitPoints:Int = 10
	def dead:Boolean = (hitPoints <= 0)
	def damage:Boolean = {
		if (Math.random > defense) {
			hitPoints = hitPoints-1
			true
		} else {
			false
		}
	}
	/**
	 * Attack another entity.
	 * This method is responsible for subtracting hit points and
	 * applying any other effects to the target, but should not
	 * despawn the target if its hit points drop to 0.
	 */
	def attack(target:EntityLiving):Boolean = {
		def attackTimes(remaining:Double):Boolean = {
			if (remaining > 1) {
				target.damage
				attackTimes(remaining-1)
				true
			} else if (remaining > 0) {
				// if fraction of an attack remains roll to see if it fails
				if (Math.random < remaining) {
					target.damage
					true
				} else {
					false
				}
			} else {
				false
			}
		}
		attackTimes(attackStrength)
	}
	def attackStrength:Double = 1.0
}

class EntityPlayer(val player:Player) extends EntityLiving {
	override def defense = player.armor.map {_.defenseModifier} getOrElse 0.0
	override def attackStrength = player.weapon map {1+_.attackModifier} getOrElse 1.0
}

abstract class EntityMob extends EntityLiving {
	def ai:AI
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		if (!dead) ai.tick(world, coords)
	}
}

abstract class EntityAnimal extends EntityMob {
	hitPoints = 5
	def ai:AI = new AIAnimal
}

abstract class EntityMonster extends EntityMob {
	def ai:AI = new AIMonster
}

class EntityPig extends EntityAnimal {
	override def drops = Seq(ItemStack(new Food(), Some(Random nextInt 4)))
}

class EntitySpider extends EntityMonster {
	hitPoints = 1
	override def defense = 0.5
	override def drops = Seq(ItemStack(new Food(), Some(Random nextInt 3)))
}

class EntityGoblin extends EntityMonster {
	override def drops = Seq(ItemStack(new Food(), Some(Random nextInt 10)))
}

class EntityDragon extends EntityMonster {
	hitPoints = 250
	override def drops = Seq(ItemStack(new EntityBlock(Diamond), Some(Random nextInt 10 + 10)))
}
