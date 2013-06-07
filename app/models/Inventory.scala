package models

import play.api.Logger
import scala.collection.Set
import scala.util.control.Breaks._

// FIXME: selected should be a property of the player
case class Inventory(var items:Seq[Item] = Seq.empty[Item], var selected:Option[Int] = None) {

	// FIXME: for testing!
	items = Seq(
		Item("pick", Some(1), Some(Wood)),
		Item("stone", Some(74), Some(Stone.BASALT)),
		Item("workbench", Some(1))
	)

	/** Returns true if user has the given item (at least as many for item stacks) */
	def has(other:Item):Boolean = {
		val item:Item = items.filter({_.subtractableFrom(other)}).headOption.getOrElse(null)
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
	def add(other:Item):Unit = {
		other.count map { c:Int =>
			if (c <= 0) return
		}
		if (other.count == None || items.filter({_.stacksWith(other)}).isEmpty) {
			items = items ++ Seq(other)
		} else if (other.count.get > 0) {
			val existing:Item = items.filter({_.stacksWith(other)}).head
			val existingIndex:Int = items.indexOf(existing)
			val updated:Item = (existing + other).get
			items = items.patch(existingIndex, Seq(updated), 1)
		}
	}
	
	/**
	 * Subtract an item. Handles item stacks and updating the selected item index.
	 */
	def subtract(other:Item):Boolean = {
		// note: (for now) if the other item has no material it will
		// subtract from the first item stack of the same kind.
		items.filter({ item:Item =>
			item.subtractableFrom(other)
		}).headOption.map({ item:Item =>
			val updated:Item = (item - other).head
			if (updated.count.get > 0) {
				// replace
				items = items.patch(items.indexOf(item), Seq(updated), 1)
				true
			} else {
				// subtract
				items = items.filter({_ != item})
				true
			}
		}).headOption.getOrElse({
			if (other.count.isEmpty && items.contains(other)) {
				items = items.patch(items.indexOf(other), Seq(), 1)
				true
			} else {
				false
			}
		})
	}
}
