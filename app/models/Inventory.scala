package models

import play.api.Logger
import scala.collection.Set
import scala.util.control.Breaks._

case class Inventory(var items:Seq[Item] = Seq.empty[Item], var selected:Option[Int] = None) {

	/** Returns true if user has the given item (at least as many for item stacks) */
	def has(other:Item):Boolean = {
		val item:Item = items.filter({_.kind == other.kind}).headOption.getOrElse(null)
		if (item == null) {
			return false
		} else if (item.count.isDefined && other.count.isDefined) {
			return item.count.get >= other.count.get
		} else {
			return true
		}
	}

	/**
	 * Add an item. Handles merging with other item stacks.
	 */
	def add(other:Item) = {
		if (other.count == None || items.filter({_.kind == other.kind}).isEmpty) {
			items = items ++ Seq(other)
		} else {
			val existing:Item = items.filter({_.kind == other.kind}).head
			val existingIndex:Int = items.indexOf(existing)
			val updated:Item = (existing + other).get
			items = items.patch(existingIndex, Seq(updated), 1)
		}
	}
	
	/**
	 * Subtract an item. Handles item stacks and updating the selected item index.
	 */
	def subtract(other:Item):Boolean = {
		if (items.filter({_.kind == other.kind}).isEmpty) {
			return false
		} else if (other.count == None) {
			// TODO: Implement removing non-stackable items
			return false
		} else {
			val existing:Option[Item] = items.filter({ item:Item => 
				(item.kind == other.kind) && (item.count.get >= other.count.get)
			}).headOption
			val existingIndex = items.indexOf(existing.get)
			if (existing.isDefined) {
				val updated:Item = (existing.get - other).head
				if (updated.count.get > 0) {
					// replace
					// FIXME: rearranges list
					items = items.patch(existingIndex, Seq(updated), 1)
				} else {
					// subtract
					items = items.filter({_.kind != other.kind})
					if (selected != None && existingIndex >= selected.get) {
						selected = Some(selected.get - 1)
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}
}

case class Item(val kind:String, val count:Option[Int] = None) {
	def +(other:Item):Option[Item] = {
		if (count == None || other.count == None) {
			// count of None means an item is not stackable
			return None
		} else {
			kind match {
				// only stack the same kind of item
				case other.kind => Some(Item(kind, Some(count.head + other.count.head)))
				case _    => None
			}
		}
	}
	def -(other:Item):Option[Item] = {
		if (count == None || other.count == None) {
			// count of None means an item is not stackable
			return None
		} else {
			kind match {
				// only stack the same kind of item
				// FIXME: return None if it would result in a negative item count
				case other.kind => Some(Item(kind, Some(count.head - other.count.head)))
				case _    => None
			}
		}
	}
}
