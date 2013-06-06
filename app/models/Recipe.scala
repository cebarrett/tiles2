package models

abstract trait Recipe {
	def result:Item
	def ingredients:Seq[Item]
}

case class WorkbenchRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe {
	override def toString():String = {
		var str:String = "Craft " + 
		result.count.map({_.toString}).getOrElse("a") +
		" " + 
		result.kind +
		" from ";

		for (i <- 0 until ingredients.length) {
			val item:Item = ingredients(i)
			if (i > 0) {
				str = str + ", "
			}
			str = str + (item.count.map({_.toString}).getOrElse("a") + " " + item.kind)
		}

		return str
	}
}

case object WorkbenchRecipe {
	// XXX: list has to be updated when a recipe is added. better way?
	def ALL_RECIPES = Seq[WorkbenchRecipe](WOOD, WOODEN_AXE)

	def WOOD = WorkbenchRecipe(Item("wood", Some(1)), Seq(Item("log", Some(1))))
	def WOODEN_AXE = WorkbenchRecipe(Item("wooden axe", Some(1)), Seq(Item("wood", Some(1)), Item("stick", Some(1))))
}