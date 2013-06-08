package models

sealed trait Material {
	val kind:String
	val color:String
	val category:String
}

sealed abstract class BaseMaterial(val kind:String = "none", val color:String = "white", val category:String = "none") extends Material {

}

case object Wood extends BaseMaterial(kind = "wood", color = "brown", category = "wood")

sealed case class Stone (override val kind:String, override val color:String) extends BaseMaterial(category = "stone")
object Stone {
	def ALL = {
		// TODO: use reflection to get all the fields
		Seq(CHALK, LIMESTONE, GRANITE, BASALT)
	}
	def CHALK     = Stone("chalk",     "#bbb")
	def LIMESTONE = Stone("limestone", "#999")
	def GRANITE   = Stone("granite",   "#666")
	def BASALT    = Stone("basalt",    "#444")
}

sealed case class Metal (override val kind:String, override val color:String) extends BaseMaterial(category = "metal")
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
