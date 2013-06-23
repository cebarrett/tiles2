package models

/**
 * An Item is anything that can be contained in an ItemStack,
 * and thus held in a player's inventory.
 */
trait Item {
	def kind:String = {
		this.getClass().getSimpleName().replaceAll("^(?:Item|Entity)", "").toLowerCase()
	}
}

case class Charcoal() extends Item
case class Log() extends Item

/**
 * A Tool is an Item that has some effect or use when held,
 * such as enabling the player to pick up certain blocks.
 * It must be crafted from a material.
 */
trait Tool extends Item {
	def material:Material
}

case class Pick(override val material:Material) extends Tool {
	
}

case class Hammer(override val material:Material) extends Tool {
	
}

case class Axe(override val material:Material) extends Tool {
	
}
