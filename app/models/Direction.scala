package models

sealed abstract trait Direction {
	def x:Int
	def y:Int
}

object North extends Direction {
	override val x =  0
	override val y =  1
}
object East  extends Direction {
	override val x =  1
	override val y =  0
}
object South extends Direction {
	override val x =  0
	override val y = -1
}
object West  extends Direction {
	override val x = -1
	override val y =  0
}
