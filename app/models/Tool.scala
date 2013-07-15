package models

import play.api.{Logger => log}

abstract class Tool(override val material:Material)
extends AbstractItemWithMaterial(material) with Entity
{
	def attackModifier = 0.0
	def defenseModifier = 0.0
	def toolStrength = (material.weight + material.hardness) / 2
	
	def tryToBreak(entity:Entity):Boolean = {
		entity match {
			case nonliving:Item => {
				if (nonliving.canBeBrokenBy(Some(this))) {
					val save = (0.7 + nonliving.defense) - toolStrength
					val retv = Math.random > save
					retv
				} else {
					false
				}
			}
			case _ => false
		}
	}
}

class Pick(override val material:Material) extends Tool(material) {
	override def attackModifier = (material.weight + material.hardness) / 3
	override def toolStrength = material.hardness
}

class Hammer(override val material:Material) extends Tool(material) {
	override def attackModifier = (material.weight * 2) / 3
	override def toolStrength = material.weight
}

class Axe(override val material:Material) extends Tool(material) {
	override def attackModifier = (material.weight + material.hardness) / 3
}

class Armor(override val material:Material) extends Tool(material) {
	// note: this is both the player's defense and the
	// entity's defense when placed on a tile.
	override def defenseModifier = material.hardness
}

class Sword(override val material:Material) extends Tool(material) {
	override def attackModifier = material.hardness
}
