package models

sealed abstract class Entity {
	def id:String
}

sealed abstract class EntityLiving extends Entity

case class EntityPlayer(val playerName:String, val id:String = "player") extends EntityLiving

sealed abstract class EntityMob extends EntityLiving {
	def ai:AI
}

case class EntityLlama(val id:String = "llama", val ai:AI = AI.Animal) extends EntityMob

/*
 * FIXME: too many entities that just correspond 1-1 with an item
 * and don't do anything else. need either EntityItem or ItemEntity?
 * better: make an entity+item derived from a material.
 */

case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWood(val id:String = "wood") extends Entity  // FIXME: remove
case class EntityStone(val material:Stone, val id:String = "stone") extends Entity
case class EntityOre(val id:String = "ore") extends Entity
case class EntitySapling(val id:String = "sapling") extends Entity

case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityFurnace(val id:String = "furnace") extends Entity
case class EntitySawmill(val id:String = "sawmill") extends Entity
case class EntityStonecutter(val id:String = "stonecutter") extends Entity