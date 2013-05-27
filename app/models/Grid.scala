package models

case class Grid[T](val length:Int, val generator: () => T) {
/*
	private val Array[Array[T]] = new A

	def get(val x:Int, val y:Int) {

	}
*/
}