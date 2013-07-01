package models

import scala.collection.Seq

case class Player (val name:String, var x:Int, var y:Int, var inventory:Inventory = new Inventory) {

	def isItemSelected:Boolean = {
		return (
			inventory.selected.isDefined &&
			inventory.selected.get >= 0 &&
			inventory.selected.get < inventory.items.size
		)
	}

	def getSelectedItem():Option[ItemStack] = {
		return inventory.selected.map({ index:Int =>
			if (index >= 0 && index < inventory.items.length) {
				Some(inventory.items(index))
			} else {
				None
			}
		}).getOrElse[Option[ItemStack]](None)
	}

	def isHoldingItem(kind:String):Boolean = {
		return getSelectedItem map {_.item.kind == kind} getOrElse false
	}
	
	def armor = headItem(classOf[Armor])
	def sword = headItem(classOf[Sword])
	
	def headItem[T <: Item](clazz:Class[T]):Option[T] = {
		inventory.items.filter({
			clazz isInstance _.item
		}).headOption.map({
			clazz cast _.item
		})
	}
	
	def pos:WorldCoordinates = WorldCoordinates(x, y)
}
