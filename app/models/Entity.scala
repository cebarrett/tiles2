package models

abstract trait Entity {
	def id:String
}

case class EntityPlayer(val playerName:String, val id:String = "player") extends Entity
case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityWood(val id:String = "wood") extends Entity
case class EntitySapling(val id:String = "sapling") extends Entity
case class EntityLlama(val id:String = "llama") extends Entity
