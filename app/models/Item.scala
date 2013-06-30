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

/**
 * An ItemWithMaterial is an item made out of a Material,
 * whose color (and in the future other properties) are
 * determined by that material.
 */
trait ItemWithMaterial extends Item {
	def material:Material
	def copyWithMaterial(material:Material):ItemWithMaterial
}

abstract class AbstractItemWithMaterial(val material:Material) extends ItemWithMaterial {
	def copyWithMaterial(material:Material) = {
		val copy = getClass getConstructor classOf[Material] newInstance(material)
		copy.asInstanceOf[ItemWithMaterial]
	}
}

case class Floor(override val material:Material) extends AbstractItemWithMaterial(material) with Terrain
case class Door (override val material:Material) extends AbstractItemWithMaterial(material) with Terrain

/**
 * A Tool is an Item that has some effect or use when held,
 * such as enabling the player to pick up certain blocks.
 * It must be crafted from a material.
 */
trait Tool extends ItemWithMaterial {
	
}

abstract class AbstractTool(override val material:Material) extends AbstractItemWithMaterial(material)

case class Pick(override val material:Material) extends AbstractTool(material) {
	
}

case class Hammer(override val material:Material) extends AbstractTool(material) {
	
}

case class Axe(override val material:Material) extends AbstractTool(material) {
	
}

/**
 * TODO: item that protects against damage when worn
 */
case class Armor(override val material:Material) extends AbstractItemWithMaterial(material) {
	
}