package models

case class Recipe(result:Item, ingredients:Seq[Item])

object Recipe {
	val all = Seq( // XXX: change from Seq to Map
		"workbench" -> Seq[Recipe](
			Recipe(Item("wood",        Some(5)),              Seq(Item("log", Some(1)))),
			Recipe(Item("axe",         None,     Some(Wood)), Seq(Item("wood", Some(5)))),
			Recipe(Item("hammer",      None,     Some(Wood)), Seq(Item("wood", Some(10)))),
			Recipe(Item("pick",        None,     Some(Wood)), Seq(Item("wood", Some(20)))),
			Recipe(Item("workbench",   Some(1)),              Seq(Item("wood", Some(25)))),
			Recipe(Item("kiln",        Some(1)),              Seq(Item("rock", Some(25)))),
			Recipe(Item("smelter",     Some(1)),              Seq(Item("rock", Some(25)))),
			Recipe(Item("sawmill",     Some(1)),              Seq(Item("wood", Some(50)))),
			Recipe(Item("stonecutter", Some(1)),              Seq(Item("rock", Some(50))))
		),
		"kiln" -> Seq[Recipe](
			Recipe(Item("charcoal", Some(1)), Seq(Item("log", Some(1))))
		),
		"smelter" -> (Seq(Metal.COPPER, Metal.IRON, Metal.SILVER, Metal.GOLD) map { metal:Metal =>
			Recipe(Item("bar", Some(1), Some(metal)), Seq(Item("ore", Some(1), Some(metal)), Item("charcoal", Some(1))))
		}),
		"sawmill" -> Seq[Recipe](
			Recipe(Item("wood", Some(1)), Seq(Item("sapling", Some(1))))
		),
		"stonecutter" -> Seq[Recipe](
			Recipe(Item("sword", None, Some(Stone.GRANITE)), Seq(Item("rock", Some(20), Some(Stone.GRANITE))))
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

