package models

abstract trait Entity {
	def id:String
}

case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWood(val id:String = "wood") extends Entity
case class EntityStone(val id:String = "stone") extends Entity
case class EntityOre(val material:Material, val id:String = "ore") extends Entity
case class EntitySapling(val id:String = "sapling") extends Entity

case class EntityLlama(val id:String = "llama") extends Entity
case class EntityPlayer(val playerName:String, val id:String = "player") extends Entity

case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityFurnace(val id:String = "furnace") extends Entity
case class EntitySawmill(val id:String = "sawmill") extends Entity
case class EntityStonecutter(val id:String = "stonecutter") extends Entity