package models

import play.api.Logger
import scala.collection.Set
import scala.util.control.Breaks._

case class Inventory(var items:Seq[Item] = Seq.empty[Item]) {

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

	def add(other:Item) = {
		Logger.debug(s"add: $other")
		if (other.count == None || items.filter({_.kind == other.kind}).isEmpty) {
			items = items ++ Seq(other)
		} else {
			val existing = items.filter({_.kind == other.kind}).head
			val updated:Item = (existing + other).head
			items = items.filter({_ != existing}).++(Seq(updated))
		}
	}
	
	def subtract(other:Item):Boolean = {
		Logger.debug(s"subtract: $other")
		if (items.filter({_.kind == other.kind}).isEmpty) {
Logger.debug("'''''empty")
			return false
		} else if (other.count == None) {
			// TODO: Implement removing non-stackable items
Logger.debug("''''' not stackable")
			return false
		} else {

Logger.debug("''''' stackable, trying to subtract")
			val existing:Option[Item] = items.filter({ item:Item => 
				(item.kind == other.kind) && (item.count.get >= other.count.get)
			}).headOption
			if (existing.isDefined) {
				val updated:Item = (existing.get - other).head
				Logger.debug("***** updated count: " + updated.count.get)
				items = items.filter({_.kind != other.kind})
				if (updated.count.get > 0) {
					items = items :+ updated
				}
				return true;
			} else {

Logger.debug("''''' ")
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
