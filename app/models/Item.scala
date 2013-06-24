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

/**
 * An ItemWithMaterial is an item made out of a Material,
 * whose color (and in the future other properties) are
 * determined by that material.
 */
trait ItemWithMaterial extends Item {
	def material:Material
}

/**
 * A Tool is an Item that has some effect or use when held,
 * such as enabling the player to pick up certain blocks.
 * It must be crafted from a material.
 */
trait Tool extends ItemWithMaterial

case class Pick(val material:Material) extends Tool {
	
}

case class Hammer(val material:Material) extends Tool {
	
}

case class Axe(val material:Material) extends Tool {
	
}
