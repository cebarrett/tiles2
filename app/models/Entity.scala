package models

import scala.util.control.Breaks._
import scala.util.Random

sealed abstract trait Entity {
	def kind:String
	def tick(world:World, coords:WorldCoordinates):Unit = {
		// no-op by default
	}
}

sealed abstract class EntityLiving extends Entity {
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

case class EntityPlayer(val player:Player, val kind:String = "player") extends EntityLiving {
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

case class EntityLlama(val kind:String = "llama") extends EntityAnimal

case class EntityGoblin(val kind:String = "goblin") extends EntityMonster


/*
 * FIXME: too many entities that just correspond 1-1 with an item
 * and don't do anything else. make an EntityItem or an ItemEntity
 */

case class EntitySapling(val kind:String = "sapling") extends Entity {
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

case class EntityTree(val species:String = "oak", val kind:String = "tree") extends Entity
case class EntityStone(val material:Stone, val kind:String = "stone") extends Entity
case class EntityOre(val material:Metal, val kind:String = "ore") extends Entity
case class EntityBlock(val material:Material, val kind:String = "block") extends Entity

case class EntityWorkbench(val kind:String = "workbench") extends Entity
case class EntityKiln(val kind:String = "kiln") extends Entity
case class EntitySmelter(val kind:String = "smelter") extends Entity
case class EntitySawmill(val kind:String = "sawmill") extends Entity
case class EntityStonecutter(val kind:String = "stonecutter") extends Entity
case class EntityAnvil(val kind:String = "anvil", val material:Material = Metal.IRON) extends Entity
