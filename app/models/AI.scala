package models

object AI {
	def Animal:AI = new AI()
}

sealed case class AI(val foo:String = "bar")