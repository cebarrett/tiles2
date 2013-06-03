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

}
