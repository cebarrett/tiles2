package models

import scala.collection.Seq

class Player (val name:String) {
	
	val inventory:Inventory = new Inventory
	var selected:Option[Int] = None
	
	def isItemSelected:Boolean = {
		return (
			selected.isDefined &&
			selected.get >= 0 &&
			selected.get < inventory.items.size
		)
	}

	def getSelectedItem():Option[ItemStack] = {
		return selected.map({ index:Int =>
			if (index >= 0 && index < inventory.items.length) {
				Some(inventory.items(index))
			} else {
				None
			}
		}).getOrElse[Option[ItemStack]](None)
	}
	
	def getSelectedTool():Option[Tool] = {
		getSelectedItem.map({_.item}) match {
			case Some(tool:Tool) => Some(tool)
			case _ => None
		}
	}

	def isHoldingItem(kind:String):Boolean =
		getSelectedItem map {_.item.kind == kind} getOrElse false
		
	def give(items:Seq[ItemStack]):Unit = items foreach { inventory.add(_) }
	
	/** Get currently equipped armor (currently always the best armor) */
	def armor:Option[Armor] = inventory.items.filter({
		_.item.isInstanceOf[Armor]
	}).sortBy({ stack =>
		(1.0 / (stack.item.asInstanceOf[Armor].defenseModifier))
	}).map({_.item.asInstanceOf[Armor]}).headOption;
	
	/** Get currently equipped sword (currently always the best sword) */
	def weapon:Option[Tool] = inventory.items.filter({
		_.item.isInstanceOf[Tool]
	}).sortBy({ stack =>
		(1.0 / (stack.item.asInstanceOf[Tool].attackModifier))
	}).map({_.item.asInstanceOf[Tool]}).headOption;
	
	def headItem[T <: Item](clazz:Class[T]):Option[T] = {
		inventory.items.filter({
			clazz isInstance _.item
		}).headOption.map({
			clazz cast _.item
		})
	}
}
