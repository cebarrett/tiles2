package models

import scala.util.control.Breaks._
import scala.collection.mutable

case class Recipe(result:ItemStack, ingredients:Seq[Ingredient]) {
	def craft(inventory:Inventory):Boolean = {
		// pair each ingredient with the first item that matches
		val reagents = new mutable.HashMap[Ingredient,Option[ItemStack]]
		ingredients foreach { i => reagents.put(i, i.find(inventory).map(_.copy(count = Some(i.count))))}
		
		// if any reagent does not have a matching item, fail
		reagents.values map {opt => if (opt.isEmpty) return false}
		
		// otherwise, subtract reagent from inventory, then add the result
		reagents.values map {_ map {inventory subtract _}}
		inventory add result
		true
	}
}

trait Ingredient {
	def count:Int = 1
	def find(inventory:Inventory):Option[ItemStack]
}

case class IngredientItem(val item:models.Item, override val count:Int = 1) extends Ingredient {
	def find(inventory:Inventory):Option[ItemStack] = {
		inventory.items.map({ stack =>
			// map to some item stack if it is $count or more of this item
			if (stack.item == item && stack.count.isDefined && stack.count.get >= count)
				Some(stack)
			else
				None
		}).filter({_.isDefined}).headOption.getOrElse(None)
	}
}

/** 
 * An ingredient that matches any block of the given class of material. 
 */
case class IngredientMaterial[T <: Material](val material:Class[T], override val count:Int = 1) extends Ingredient {
	def find(inventory:Inventory):Option[ItemStack] = {
		inventory.items.map({ stack =>
			// map to some item stack if it is $count or more blocks of this ingredient
			stack.item match {
				case block:EntityBlock => {
					if (material.isInstance(block.material) && stack.count.isDefined && stack.count.get >= count)
						Some(stack)
					else
						None
				}
				case _ => None
			}
		}).filter({_.isDefined}).headOption.getOrElse(None)
	}
}

object Recipe {
	val all = Map(
		"workbench" -> Seq[Recipe](
			Recipe(ItemStack(Axe(Wood)),            Seq(IngredientMaterial(Wood.getClass,   5))),
			Recipe(ItemStack(Hammer(Wood)),         Seq(IngredientMaterial(Wood.getClass,  10))),
			Recipe(ItemStack(Pick(Wood)),           Seq(IngredientMaterial(Wood.getClass,  25))),
			Recipe(ItemStack(EntityWorkbench()),    Seq(IngredientMaterial(Wood.getClass,  25))),
			Recipe(ItemStack(EntityKiln()),         Seq(IngredientMaterial(classOf[Stone], 25))),
			Recipe(ItemStack(EntitySmelter()),      Seq(IngredientMaterial(classOf[Stone], 25))),
			Recipe(ItemStack(EntitySawmill()),      Seq(IngredientMaterial(classOf[Stone], 50))),
			Recipe(ItemStack(EntityStonecutter()),  Seq(IngredientMaterial(classOf[Stone], 50))),
			Recipe(ItemStack(EntityAnvil()),        Seq(IngredientMaterial(Iron.getClass,  20)))
		),
		"kiln" -> Seq[Recipe](
			Recipe(ItemStack(Charcoal(), Some(1)),  Seq(IngredientMaterial(Wood.getClass, 1)))
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

