package models

import play.api.Logger

case class ItemStack(val item:Item, val count:Option[Int] = Some(1)) {
	
	def +(other:ItemStack):Option[ItemStack] = {
		if (stacksWith(other)) {
			Some(ItemStack(item, Some(count.head + other.count.head)))
		} else {
			None
		}
	}
	
	def -(other:ItemStack):Option[ItemStack] = {
		if (stacksWith(other)) {
			if (count.isEmpty) {
				return Some(this.copy(count = Some(0)))
			} else {
				val newCount:Int = count.head - other.count.head
				if (newCount >= 0) {
					Some(ItemStack(item, Some(newCount)))
				} else {
					None
				}
			}
		} else {
			None
		}
	}
	
	def stacksWith(other:ItemStack):Boolean = {
		count.isDefined &&
		other.count.isDefined && 
		count.get >= other.count.get &&
		item.kind == other.item.kind &&
		(other.item match {
			case otherItem:ItemWithMaterial => {
				this.item match {
					case thisItem:ItemWithMaterial => {
						otherItem.material.kind == thisItem.material.kind
					}
					case _ => false
				}
			}
			case _ => true
		})
	}
	
	override def toString():String = {
		(if (count.isEmpty) s"$count " else "") + s"$item"
	}
}
