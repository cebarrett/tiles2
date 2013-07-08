package models

abstract class Building(override val material:Material)
extends AbstractItemWithMaterial(material) with Entity
{
	override def defense = (material.hardness + material.weight) / 2
	override def canBeBrokenBy(tool:Option[Tool]):Boolean =
			tool map { _.isInstanceOf[Hammer] } getOrElse false
}

class EntityWorkbench(override val material:Material) extends Building(material)
class EntityKiln(override val material:Material) extends Building(material)
class EntitySmelter(override val material:Material) extends Building(material)
class EntitySawmill(override val material:Material) extends Building(material)
class EntityStonecutter(override val material:Material) extends Building(material)
class EntityAnvil(override val material:Material) extends Building(material)
class Gemcutter(override val material:Material) extends Building(material)
