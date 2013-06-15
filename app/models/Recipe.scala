package models

abstract trait Recipe {
	def result:Item
	def ingredients:Seq[Item]
	override def toString():String = {
		var str:String = "Craft " + result.toString + " from ";

		for (i <- 0 until ingredients.length) {
			val item:Item = ingredients(i)
			if (i > 0) {
				str = str + ", "
			}
			str = str + item.toString
		}

		return str
	}
}

/*
 * FIXME: each crafting station should have a list of case object recipes,
 * not its own subclass of Recipe.
 */

sealed case class WorkbenchRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
case object WorkbenchRecipe {
	// TODO: use reflection to get all the fields
	def ALL = Seq[WorkbenchRecipe](WOOD, AXE, HAMMER, PICK, WORKBENCH, KILN, SMELTER, SAWMILL, STONECUTTER)

	def WOOD = WorkbenchRecipe(Item("wood", Some(5)), Seq(Item("log", Some(1))))
	def AXE = WorkbenchRecipe(Item("axe", None, Some(Wood)), Seq(Item("wood", Some(5))))
	def HAMMER = WorkbenchRecipe(Item("hammer", None, Some(Wood)), Seq(Item("wood", Some(10))))
	def PICK = WorkbenchRecipe(Item("pick", None, Some(Wood)), Seq(Item("wood", Some(20))))
	def WORKBENCH = WorkbenchRecipe(Item("workbench", Some(1)), Seq(Item("wood", Some(25))))
	def KILN = WorkbenchRecipe(Item("kiln", Some(1)), Seq(Item("rock", Some(25))))
	def SMELTER = WorkbenchRecipe(Item("smelter", Some(1)), Seq(Item("rock", Some(25))))
	def SAWMILL = WorkbenchRecipe(Item("sawmill", Some(1)), Seq(Item("wood", Some(50))))
	def STONECUTTER = WorkbenchRecipe(Item("stonecutter", Some(1)), Seq(Item("rock", Some(50))))
}

sealed case class KilnRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
case object KilnRecipe {
	def ALL = Seq[KilnRecipe](CHARCOAL)

	def CHARCOAL = KilnRecipe(Item("charcoal", Some(1)), Seq(Item("log", Some(1))))
}

sealed case class SmelterRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
case object SmelterRecipe {
	def ALL = Seq[SmelterRecipe](COPPER_INGOT)

	def COPPER_INGOT = SmelterRecipe(Item("ingot", None, Some(Metal.COPPER)), Seq(Item("ore", Some(20), Some(Metal.COPPER))))
}

sealed case class SawmillRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
case object SawmillRecipe {
	def ALL = Seq[SawmillRecipe]()
}

sealed case class StonecutterRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
case object StonecutterRecipe {
	def ALL = Seq[StonecutterRecipe]()
}
