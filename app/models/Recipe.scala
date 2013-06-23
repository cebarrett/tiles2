package models

case class Recipe(result:ItemStack, ingredients:Seq[Ingredient]) {
	
}

trait Ingredient {
	def count:Int = 1
	def toItemStack:ItemStack
}

case class IngredientItem(val item:models.Item, override val count:Int = 1) extends Ingredient {
	def toItemStack = ItemStack(item, Some(count))
}

case class IngredientMaterial[T <: Material](val material:T, override val count:Int = 1) extends Ingredient {
	def toItemStack = ItemStack(EntityBlock(material), Some(count))
}

object Recipe {
	val all = Map(
		"workbench" -> Seq[Recipe](
			Recipe(ItemStack(Axe(Wood), None),      Seq(IngredientMaterial(Wood,  5))),
			Recipe(ItemStack(Hammer(Wood), None),   Seq(IngredientMaterial(Wood, 10))),
			Recipe(ItemStack(Pick(Wood), None),     Seq(IngredientMaterial(Wood, 25))),
			Recipe(ItemStack(EntityWorkbench()),    Seq(IngredientMaterial(Wood, 25))),
			Recipe(ItemStack(EntityKiln()),         Seq(IngredientMaterial(Stone, Some(25)))),
			Recipe(ItemStack(EntitySmelter()),      Seq(IngredientMaterial(Stone, Some(25)))),
			Recipe(ItemStack(EntitySawmill()),      Seq(IngredientMaterial(Stone, Some(50)))),
			Recipe(ItemStack(EntityStonecutter()),  Seq(IngredientMaterial(Stone,, Some(50)))),
			Recipe(ItemStack(EntityAnvil()),        Seq(Ingredient(EntityBlock[Iron], Some(20))))
		),
		"kiln" -> Seq[Recipe](
			Recipe(ItemStack(Charcoal(), Some(1)), Seq(ItemStack(EntityBlock(Wood), Some(1))))
		),
		"smelter" -> Seq[Recipe](

		),
		"sawmill" -> Seq[Recipe](

		),
		"stonecutter" -> Seq[Recipe](

		),
		"anvil" -> Seq[Recipe](

		)
	)

	def kind(craft:String):Seq[Recipe] = {
		all filter { tuple:(String, Seq[Recipe]) =>
			val (otherCraft:String, recipes:Seq[Recipe]) = tuple
			if (craft == otherCraft) true else false
		} map { tuple:(String, Seq[Recipe]) =>
			val (kind, recipes) = tuple
			recipes
		} head
	}

	def get(craft:String, index:Int):Recipe = {
		kind(craft)(index)
	}
}

