package models

import play.api.Logger


/**
 * An Item is anything that can be contained in an ItemStack,
 * and thus held in a player's inventory.
 */
trait Item {
	def kind = this.getClass().getSimpleName().replaceAll("^(?:Item|Entity)|\\$*$", "").toLowerCase()
	def defense = 0.0
	def toItemStack = ItemStack(this)
}

trait ItemWithMaterial extends Item {
	def material:Material
	def copyWithMaterial(material:Material) = {
		val copy = getClass getConstructor classOf[Material] newInstance(material)
		copy.asInstanceOf[ItemWithMaterial]
	}
}

abstract class AbstractItemWithMaterial(val material:Material) extends ItemWithMaterial with Entity {
	override def drops = Seq(ItemStack(this))
	override def canBeBrokenBy(tool:Option[Tool]) = true
}

class Floor(override val material:Material) extends ItemWithMaterial with Terrain
// XXX: shouldn't be able to place entities on doors (or water, or lava...)
class Door (override val material:Material) extends ItemWithMaterial with Terrain

class Mechanism(override val material:Material) extends AbstractItemWithMaterial(material)