package models

sealed abstract trait Direction {
	def x:Int
	def y:Int
}

case object North extends Direction {
	override val x =  0
	override val y =  1
}
case object East  extends Direction {
	override val x =  1
	override val y =  0
}
case object South extends Direction {
	override val x =  0
	override val y = -1
}
case object West  extends Direction {
	override val x = -1
	override val y =  0
}
