package models

trait Item {
	def material:Material = Cheese
}

case class Charcoal() extends Item
case class Log() extends Item

trait Tool extends Item

case class Pick(val material:Material) extends Tool {
	
}

case class Hammer(val material:Material) extends Tool {
	
}

case class Axe(val material:Material) extends Tool {
	
}
