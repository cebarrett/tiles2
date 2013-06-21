package models

case class ItemStack(val kind:String, val count:Option[Int] = None, val material:Option[Material] = None) {
	def +(other:ItemStack):Option[ItemStack] = {
		if (stacksWith(other)) {
			Some(ItemStack(kind, Some(count.head + other.count.head), material))
		} else {
			None
		}
	}
	def -(other:ItemStack):Option[ItemStack] = {
		if (subtractableFrom(other)) {
			val newCount:Int = count.head - other.count.head
			if (newCount >= 0) {
				Some(ItemStack(kind, Some(newCount), material));
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
			kind == other.kind &&
			material == other.material
		)
	}
	def subtractableFrom(other:ItemStack):Boolean = {
		return (
			((count == None && other.count == None) || (count.isDefined && other.count.isDefined && (count.get >= other.count.get))) &&
			(kind == other.kind) &&
			( ! (other.material.isDefined && other.material != material) )
		)
	}
	override def toString():String = {
		var string:String = ""
		count map    { c => string = string + c + " " }
		material map { m => string = string + m.kind + " " }
		string = string + kind
		string
	}
}
