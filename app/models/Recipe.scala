package models

abstract trait Recipe {
	def result:Item
	def ingredients:Seq[Item]
}

// case object WorkbenchRecipe = {
// 	// TODO: list of valid recipes
// }

case class WorkbenchRecipe(val result:Item, val ingredients:Seq[Item]) extends Recipe
