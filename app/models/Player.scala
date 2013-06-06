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

	def getSelectedItem():Option[Item] = {
		return inventory.selected.map({ index:Int =>
			if (index >= 0 && index < inventory.items.length) {
				Some(inventory.items(index))
			} else {
				None
			}
		}).getOrElse[Option[Item]](None)
	}

	def isHoldingItem(kind:String):Boolean = {
		return getSelectedItem map {_.kind == kind} getOrElse false
	}

}
