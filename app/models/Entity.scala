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
}

case class Food() extends Entity

abstract class EntityLiving extends Entity {
	var hitPoints:Int = 10
	def dead:Boolean = (hitPoints <= 0)
	def defense:Double = 0.0
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
		def step(remaining:Double):Boolean = {
			if (remaining > 1) {
				target.damage
				step(remaining-1)
				true
			} else if (remaining > 0) {
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
		step(attackStrength)
	}
	def attackStrength:Double = 1.0
	def drop:Seq[ItemStack] = Seq.empty
}

case class EntityPlayer(val player:Player) extends EntityLiving {
	override def defense = player.armor.map {_.defense} getOrElse 0.0
	override def attackStrength:Double = player.sword map {_.attackStrength} getOrElse 1.0
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

case class EntityPig() extends EntityAnimal {
	override def drop = Seq(ItemStack(Food(), Some(Random nextInt 1 + 1)))
}

case class EntitySpider() extends EntityMonster {
	hitPoints = 1
	override def defense = 0.5
	override def drop = Seq(ItemStack(Food(), Some(Random nextInt 2 + 2)))
}

case class EntityGoblin() extends EntityMonster {
	override def drop = Seq(ItemStack(Food(), Some(Random nextInt 3 + 3)))
}

case class EntityDragon() extends EntityMonster {
	hitPoints = 250
	override def drop = Seq(ItemStack(EntityBlock(Diamond), Some(Random nextInt 10 + 10)))
}

case class EntitySapling() extends Entity {
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		val tile:Tile = world.tileAt(coords)
		val chanceOfTreeGrowing:Double = 0.0001;
		if (Math.random() < chanceOfTreeGrowing) {
			tile.entity = Some(EntityTree())
			// XXX: next line seems out of place
			world.broadcastTileEvent(coords)
		}
	}
}

/**
 * A Block is a kind of entity made out of a material.
 * It can be placed to occupy a tile, and is also used
 * in crafting recipes that require a material ingredient.
 */
case class EntityBlock(override val material:Material) extends AbstractItemWithMaterial(material) with Entity

case class EntityTree() extends Entity
case class EntityWorkbench(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class EntityKiln(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class EntitySmelter(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class EntitySawmill(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class EntityStonecutter(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class EntityAnvil(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
case class Gemcutter(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
