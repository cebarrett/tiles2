package models

abstract trait Entity {
	def id:String
}

abstract trait EntityMob extends Entity

case class EntityPlayer(val playerName:String, val id:String = "player") extends Entity
case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWorkbench(val id:String = "workbench") extends Entity
