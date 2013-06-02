package models

import play.api.Logger
import scala.collection.Set

case class Inventory(var items:Seq[Item] = Seq.empty[Item]) {
	def add(other:Item) = {
		if (other.count == None || items.filter({_.kind == other.kind}).isEmpty) {
			items = items ++ Seq(other)
		} else {
			val existing = items.filter({_.kind == other.kind}).head
			val updated:Item = (existing + other).head
			items = items.filter({_ != existing}).++(Seq(updated))
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
}
