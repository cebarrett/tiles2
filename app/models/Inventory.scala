package models

import play.api.Logger
import scala.collection.Set
import scala.util.control.Breaks._

// FIXME: selected should be a property of the player
case class Inventory(var items:Seq[ItemStack] = Seq.empty[ItemStack], var selected:Option[Int] = None) {

	// starting inventory for dev testing
	items = if (Game.DEV) Seq(
		ItemStack(EntityWorkbench()),
		ItemStack(Axe(Gold)),
		ItemStack(Pick(Gold)),
		ItemStack(Hammer(Gold)),
		ItemStack(EntityBlock(Wood), Some(250)),
		ItemStack(EntityBlock(Iron), Some(250)),
		ItemStack(EntityBlock(Sandstone), Some(250))
	) else Seq(
		ItemStack(Axe(Granite)),
		ItemStack(EntityWorkbench())
	)

	/** Returns true if user has the given item (at least as many for item stacks) */
	def has(other:ItemStack):Boolean = {
		val item:ItemStack = items.filter({_.subtractableFrom(other)}).headOption.getOrElse(null)
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
	def add(other:ItemStack):Unit = {
		other.count map { c:Int =>
			if (c <= 0) return
		}
		if (other.count == None || items.filter({_.stacksWith(other)}).isEmpty) {
			items = items ++ Seq(other)
		} else if (other.count.get > 0) {
			val existing:ItemStack = items.filter({_.stacksWith(other)}).head
			val existingIndex:Int = items.indexOf(existing)
			val updated:ItemStack = (existing + other).get
			items = items.patch(existingIndex, Seq(updated), 1)
		}
	}
	
	/**
	 * Subtract an item. Handles item stacks and updating the selected item index.
	 */
	def subtract(other:ItemStack):Option[ItemStack] = {
		// note: (for now) if the other item has no material it will
		// subtract from the first item stack of the same kind.
		items.filter({ item:ItemStack =>
			// filter itemstacks from which other can be subtracted
			item.subtractableFrom(other)
		}).headOption.map({ item:ItemStack =>
			// handle stacks with count
			val updated:ItemStack = (item - other).head
			if (updated.count.get > 0) {
				// replace
				items = items.patch(items.indexOf(item), Seq(updated), 1)
			} else {
				// subtract
				items = items.filter({_ != item})
			}
			Some(other)
		}).headOption.getOrElse({
			// handle stacks with no count
			if (other.count.isEmpty && items.contains(other)) {
				items = items.patch(items.indexOf(other), Seq(), 1)
				Some(other)
			} else {
				None
			}
		})
	}
	
	def subtractOneOf(other:ItemStack):Option[ItemStack] = {
		if (other.count isEmpty) {
			subtract(other)
		} else {
			subtract(other.copy(count = Some(1)))
		}
	}
}
