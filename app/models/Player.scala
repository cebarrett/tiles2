package models

import scala.collection.Seq

case class Player (val name:String, var x:Int, var y:Int, val inventory:Inventory = new Inventory, var selected:Option[Int] = None) {
	
	def pos = WorldCoordinates(x, y)
	
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

	def isHoldingItem(kind:String):Boolean =
		getSelectedItem map {_.item.kind == kind} getOrElse false
	
	/** Get currently equipped armor (currently always the best armor) */
	def armor:Option[Armor] = inventory.items.filter({
		_.item.isInstanceOf[Armor]
	}).sortBy({ stack =>
		(1.0 / (stack.item.asInstanceOf[Armor].defense))
	}).map({_.item.asInstanceOf[Armor]}).headOption;
	
	/** Get currently equipped sword (currently always the best sword) */
	def sword:Option[Sword] = inventory.items.filter({
		_.item.isInstanceOf[Sword]
	}).sortBy({ stack =>
		(1.0 / (stack.item.asInstanceOf[Sword].attackStrength))
	}).map({_.item.asInstanceOf[Sword]}).headOption;
	
	def headItem[T <: Item](clazz:Class[T]):Option[T] = {
		inventory.items.filter({
			clazz isInstance _.item
		}).headOption.map({
			clazz cast _.item
		})
	}
}
