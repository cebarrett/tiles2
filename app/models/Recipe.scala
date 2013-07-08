package models

import scala.util.control.Breaks._
import scala.collection.mutable

case class Recipe(result:ItemStack, ingredients:Seq[Ingredient], useMaterial:Boolean = false) {
	def craft(inventory:Inventory):Boolean = {
		// pair each ingredient with the first item that matches
		// TODO: use the player's selected item
		val reagents = new mutable.HashMap[Ingredient,Option[ItemStack]]
		ingredients foreach { i => reagents.put(i, i.find(inventory).map(_.copy(count = Some(i.count))))}
		
		// if any reagent does not have a matching item, fail
		reagents.values map {opt => if (opt.isEmpty) return false}
		
		// otherwise, subtract reagent from inventory, then add the result.
		// substitute first ingredient's material if useMaterial is set.
		reagents.values map {_ map {inventory subtract _}}
		inventory add {
			if (!useMaterial) result else {
				val ingItem = reagents.values.head.get.item.asInstanceOf[ItemWithMaterial]
				val resultItem:Item = result.item.asInstanceOf[ItemWithMaterial] copyWithMaterial ingItem.material
				ItemStack(resultItem, result.count)
			}
		}
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
			Recipe(ItemStack(new Axe(null)),            Seq(IngredientMaterial(Wood.getClass,  10)), true),
			Recipe(ItemStack(new Hammer(null)),         Seq(IngredientMaterial(Wood.getClass,  10)), true),
			Recipe(ItemStack(new Pick(null)),           Seq(IngredientMaterial(Wood.getClass,  20)), true),
			Recipe(ItemStack(new Sword(null)),          Seq(IngredientMaterial(Wood.getClass,  20)), true),
			Recipe(ItemStack(new EntityStonecutter(null)),  Seq(IngredientMaterial(classOf[Stone], 30)), true),
			Recipe(ItemStack(new EntityKiln(null)),         Seq(IngredientMaterial(classOf[Stone], 30)), true),
			Recipe(ItemStack(new EntitySmelter(null)),      Seq(IngredientMaterial(classOf[Stone], 30)), true),
			Recipe(ItemStack(new EntitySawmill(null)),      Seq(IngredientMaterial(classOf[Stone], 30)), true)
		),
		"kiln" -> Seq[Recipe](
			Recipe(ItemStack(new EntityBlock(Charcoal), Some(1)), Seq(IngredientMaterial(Wood.getClass, 1))),
			Recipe(ItemStack(new EntityAnvil(null)), Seq(IngredientMaterial(classOf[Metal], 10)), true)
		),
		"smelter" -> Seq[Recipe](
			Recipe(ItemStack(new EntityBlock(Copper), Some(1)),  Seq(IngredientMaterial(Malachite.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Iron), Some(1)),  Seq(IngredientMaterial(Hematite.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Tin), Some(1)),  Seq(IngredientMaterial(Cassiterite.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Zinc), Some(1)),  Seq(IngredientMaterial(Sphalerite.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Titanium), Some(1)),  Seq(IngredientMaterial(Ilmenite.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Lead), Some(1)),  Seq(IngredientMaterial(Galena.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Brass), Some(2)),  Seq(IngredientMaterial(Copper.getClass, 1), IngredientMaterial(Zinc.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Bronze), Some(2)),  Seq(IngredientMaterial(Copper.getClass, 1), IngredientMaterial(Tin.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Electrum), Some(2)),  Seq(IngredientMaterial(Gold.getClass, 1), IngredientMaterial(Silver.getClass, 1), IngredientMaterial(Charcoal.getClass, 1))),
			Recipe(ItemStack(new EntityBlock(Steel), Some(1)),  Seq(IngredientMaterial(Hematite.getClass, 1), IngredientMaterial(Limestone.getClass, 1), IngredientMaterial(Charcoal.getClass, 1)))
		),
		"sawmill" -> Seq[Recipe](
			Recipe(ItemStack(new Floor(Wood), Some(2)), Seq(IngredientMaterial(Wood.getClass, 1)), true),
			Recipe(ItemStack(new Door(Wood), Some(1)),  Seq(IngredientMaterial(Wood.getClass, 5)), true),
			Recipe(ItemStack(new EntityWorkbench(null)),    Seq(IngredientMaterial(Wood.getClass, 10)), true)
		),
		"stonecutter" -> Seq[Recipe](
			Recipe(ItemStack(new Axe(null)),       Seq(IngredientMaterial(classOf[Stone], 10)), true),
			Recipe(ItemStack(new Hammer(null)),    Seq(IngredientMaterial(classOf[Stone], 10)), true),
			Recipe(ItemStack(new Pick(null)),      Seq(IngredientMaterial(classOf[Stone], 20)), true),
			Recipe(ItemStack(new Sword(null)),     Seq(IngredientMaterial(classOf[Stone], 20)), true),
			Recipe(ItemStack(new Gemcutter(null)), Seq(IngredientMaterial(Obsidian.getClass, 30)), true)
		),
		"anvil" -> Seq[Recipe](
			Recipe(ItemStack(new Axe(null)),    Seq(IngredientMaterial(classOf[Metal], 10)), true),
			Recipe(ItemStack(new Hammer(null)), Seq(IngredientMaterial(classOf[Metal], 10)), true),
			Recipe(ItemStack(new Pick(null)),   Seq(IngredientMaterial(classOf[Metal], 20)), true),
			Recipe(ItemStack(new Sword(null)),  Seq(IngredientMaterial(classOf[Metal], 20)), true),
			Recipe(ItemStack(new Armor(null)),  Seq(IngredientMaterial(classOf[Metal], 30)), true)
		),
		"gemcutter" -> Seq[Recipe](
			Recipe(ItemStack(new Axe(null)),    Seq(IngredientMaterial(classOf[Gem], 10)), true),
			Recipe(ItemStack(new Hammer(null)), Seq(IngredientMaterial(classOf[Gem], 10)), true),
			Recipe(ItemStack(new Pick(null)),   Seq(IngredientMaterial(classOf[Gem], 20)), true),
			Recipe(ItemStack(new Sword(null)),  Seq(IngredientMaterial(classOf[Gem], 20)), true),
			Recipe(ItemStack(new Armor(null)),  Seq(IngredientMaterial(classOf[Gem], 30)), true)
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

