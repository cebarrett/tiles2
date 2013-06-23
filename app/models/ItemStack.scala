package models

case class ItemStack(val item:Item, val count:Option[Int] = None) {
	
	def +(other:ItemStack):Option[ItemStack] = {
		if (stacksWith(other)) {
			Some(ItemStack(item, Some(count.head + other.count.head)))
		} else {
			None
		}
	}
	
	def -(other:ItemStack):Option[ItemStack] = {
		if (subtractableFrom(other)) {
			val newCount:Int = count.head - other.count.head
			if (newCount >= 0) {
				Some(ItemStack(item, Some(newCount)))
			} else {
				None
			}
		} else {
			None
		}
	}
	
	def stacksWith(other:ItemStack):Boolean = {
		return (
			count.isDefined &&
			other.count.isDefined &&
			item == other.item
		)
	}
	
	def subtractableFrom(other:ItemStack):Boolean = {
		return (
			(
				(count == None && other.count == None) || 
				(
					count.isDefined &&
					other.count.isDefined && 
					(count.get >= other.count.get)
				)
			) &&
			(item.kind == other.item.kind)
		)
	}
	
	override def toString():String = {
		(if (count.isEmpty) s"$count " else "") + s"$item"
	}
}
