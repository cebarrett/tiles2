package models

case class Item(val kind:String, val count:Option[Int] = None, val material:Option[Material] = None) {
	def +(other:Item):Option[Item] = {
		if (stacksWith(other)) {
			Some(Item(kind, Some(count.head + other.count.head), material))
		} else {
			None
		}
	}
	def -(other:Item):Option[Item] = {
		if (subtractableFrom(other)) {
			val newCount:Int = count.head - other.count.head
			if (newCount >= 0) {
				Some(Item(kind, Some(newCount), material));
			} else {
				None
			}
		} else {
			None
		}
	}
	def stacksWith(other:Item):Boolean = {
		return (
			count.isDefined &&
			other.count.isDefined &&
			kind == other.kind &&
			material == other.material
		)
	}
	def subtractableFrom(other:Item):Boolean = {
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
