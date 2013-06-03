package models

abstract trait Entity {
	def id:String
}

abstract trait EntityMob extends Entity

case class EntityPlayer(val playerName:String, val id:String = "player") extends Entity
case class EntityTree(val species:String = "oak", val id:String = "tree") extends Entity
case class EntityWorkbench(val id:String = "workbench") extends Entity
case class EntityWood(val id:String = "wood") extends Entity

// FIXME: find somewhere else to put this
case class WorkbenchRecipe(val result:Item, val ingredients:Seq[Item])