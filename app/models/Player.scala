package models

import scala.collection.Seq
import play.api.{Logger => log}

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
	def weapon:Option[Tool] = {
		val sword = inventory.items.filter({
				_.item.isInstanceOf[Tool]
			}).sortBy({ stack =>
				(1.0 / (stack.item.asInstanceOf[Tool].attackModifier))
			}).map({_.item.asInstanceOf[Tool]}).headOption;
		log debug s"best sword: $sword"
		sword
	}
	
	def headItem[T <: Item](clazz:Class[T]):Option[T] = {
		inventory.items.filter({
			clazz isInstance _.item
		}).headOption.map({
			clazz cast _.item
		})
	}
	
	def swapItems(i0:Int, i1:Int):Boolean = {
		if (( inventory.validate(i0) && inventory.validate(i1) )) {
			val item0 = inventory.items(i0)
			val item1 = inventory.items(i1)
			inventory.items = inventory.items.updated(i0, item1)
			inventory.items = inventory.items.updated(i1, item0)
			selected = selected match {
				case Some(i) if (i == i0) => Some(i1)
				case Some(i) if (i == i1) => Some(i0)
				case Some(_) => selected
				case None => None
			}
			true
		} else {
			false
		}
	}
}
