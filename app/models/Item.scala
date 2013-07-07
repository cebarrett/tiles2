package models

/**
 * An Item is anything that can be contained in an ItemStack,
 * and thus held in a player's inventory.
 */
trait Item {
	def kind = this.getClass().getSimpleName().replaceAll("^(?:Item|Entity)|\\$*$", "").toLowerCase()
}

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

class Floor(override val material:Material) extends AbstractItemWithMaterial(material) with Terrain
class Door (override val material:Material) extends AbstractItemWithMaterial(material) with Terrain

abstract class Tool(override val material:Material) extends AbstractItemWithMaterial(material) with Entity
class Pick(override val material:Material) extends Tool(material)
class Hammer(override val material:Material) extends Tool(material)
class Axe(override val material:Material) extends Tool(material)
class Sword(override val material:Material) extends Tool(material) {
	def attackStrength:Double = material.weight + material.hardness
}
class Armor(override val material:Material) extends Tool(material) {
	def defense = material.hardness
}
