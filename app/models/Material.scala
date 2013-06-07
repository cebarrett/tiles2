package models

case class Material(val kind:String)

object Material {
	def WOOD = Material("wood")
	def STONE = Material("stone")
	def COPPER = Material("copper")
}
