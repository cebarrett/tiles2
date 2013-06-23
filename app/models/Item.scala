package models

trait Item {
	def kind:String = {
		this.getClass().getSimpleName().replaceAll("^(?:Item|Entity)", "").toLowerCase()
	}
}

case class Charcoal() extends Item
case class Log() extends Item

trait Tool extends Item {
	def material:Material
}

case class Pick(override val material:Material) extends Tool {
	
}

case class Hammer(override val material:Material) extends Tool {
	
}

case class Axe(override val material:Material) extends Tool {
	
}
