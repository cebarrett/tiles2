package models

sealed case class Material(kind:String)
object Material {
	def WOOD = Material("wood")
	def STONE = Material("stone")
	def METAL = Material("metal")
}

sealed case class Stone (kind:String, color:String)
object Stone {
	def ALL = {
		// TODO: use reflection to get all the fields
		Seq(TALC, GRANITE, ORTHOCLASE, BASALT)
	}
	def TALC       = Stone("talc",       "#ffffff")
	def GRANITE    = Stone("granite",    "gray")
	def ORTHOCLASE = Stone("orthoclase", "rgb(180,140,120)")
	def BASALT     = Stone("basalt",     "#333")
}

sealed case class Metal (kind:String, color:String)
object Metal {
	def ALL = {
		// TODO: use reflection to get all the fields
		Seq(COPPER, IRON, SILVER, GOLD)
	}
	def COPPER       = Metal("copper", "sienna")
	def IRON         = Metal("iron",   "#987")
	def SILVER       = Metal("silver", "silver")
	def GOLD         = Metal("gold",   "gold")
}
