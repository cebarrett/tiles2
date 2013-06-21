package models

case class Recipe(result:ItemStack, ingredients:Seq[ItemStack])

object Recipe {
	val all = Seq( // XXX: change from Seq to Map
		"workbench" -> Seq[Recipe](
			Recipe(ItemStack("wood",        Some(5)),              Seq(ItemStack("log", Some(1)))),
			Recipe(ItemStack("axe",         None,     Some(Wood)), Seq(ItemStack("wood", Some(5)))),
			Recipe(ItemStack("hammer",      None,     Some(Wood)), Seq(ItemStack("wood", Some(10)))),
			Recipe(ItemStack("pick",        None,     Some(Wood)), Seq(ItemStack("wood", Some(20)))),
			Recipe(ItemStack("workbench",   Some(1)),              Seq(ItemStack("wood", Some(25)))),
			Recipe(ItemStack("kiln",        Some(1)),              Seq(ItemStack("rock", Some(25)))),
			Recipe(ItemStack("smelter",     Some(1)),              Seq(ItemStack("rock", Some(25)))),
			Recipe(ItemStack("sawmill",     Some(1)),              Seq(ItemStack("rock", Some(50)))),
			Recipe(ItemStack("stonecutter", Some(1)),              Seq(ItemStack("rock", Some(50)))),
			Recipe(ItemStack("anvil",       Some(1)),              Seq(ItemStack("bar", Some(25), Some(Metal.IRON))))
		),
		"kiln" -> Seq[Recipe](
			Recipe(ItemStack("charcoal", Some(1)), Seq(ItemStack("log", Some(1))))
		),
		"smelter" -> (Seq(Metal.COPPER, Metal.IRON, Metal.SILVER, Metal.GOLD) map { metal:Metal =>
			Recipe(ItemStack("bar", Some(1), Some(metal)), Seq(ItemStack("ore", Some(1), Some(metal)), ItemStack("charcoal", Some(1))))
		}),
		"sawmill" -> Seq[Recipe](
			Recipe(ItemStack("wood", Some(1)), Seq(ItemStack("sapling", Some(1))))
		),
		"stonecutter" -> Seq[Recipe](
			Recipe(ItemStack("sword", None, Some(Stone.GRANITE)), Seq(ItemStack("rock", Some(20), Some(Stone.GRANITE))))
		),
		"anvil" -> Seq[Recipe](
			Recipe(ItemStack("block", Some(1), Some(Metal.COPPER)), Seq(ItemStack("bar", Some(10), Some(Metal.COPPER)))),
			Recipe(ItemStack("block", Some(1), Some(Metal.IRON)),   Seq(ItemStack("bar", Some(10), Some(Metal.IRON))  )),
			Recipe(ItemStack("block", Some(1), Some(Metal.SILVER)), Seq(ItemStack("bar", Some(10), Some(Metal.SILVER)))),
			Recipe(ItemStack("block", Some(1), Some(Metal.GOLD)),   Seq(ItemStack("bar", Some(10), Some(Metal.GOLD))  ))
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

