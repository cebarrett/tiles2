package models

case class Item(val kind:String, val count:Option[Int] = None, val material:Option[Material] = None) {
	def +(other:Item):Option[Item] = {
		if (count == None || other.count == None) {
			// count of None means an item is not stackable
			return None
		} else {
			(kind, material) match {
				// only stack the same kind/material of item
				case (other.kind, other.material) => Some(Item(kind, Some(count.head + other.count.head), material))
				case (_, _) => None
			}
		}
	}
	def -(other:Item):Option[Item] = {
		if (count == None || other.count == None) {
			// count of None means an item is not stackable
			return None
		} else {
			(kind, material) match {
				// only stack the same kind of item
				case (other.kind, other.material) => {
					val newCount:Int = count.head - other.count.head
					if (newCount < 0) {
						None
					} else {
						Some(Item(kind, Some(newCount), material))
					}
				}
				case (_, _) => None
			}
		}
	}
}
