package models

sealed trait Material {
	val kind:String
	val color:String
	val category:String
}

sealed abstract class BaseMaterial(val kind:String = "none", val color:String = "white", val category:String = "none") extends Material {

}

sealed case class Wood() extends BaseMaterial(kind = "wood", color = "brown", category = "wood")

sealed case class Stone (override val kind:String, override val color:String) extends BaseMaterial(category = "stone")
object Stone {
	def ALL = {
		// TODO: use reflection to get all the fields
		Seq(TALC, GRANITE, ORTHOCLASE, BASALT)
	}
	def TALC       = Stone("talc",       "#ffffff")
	def GRANITE    = Stone("granite",    "gray")
	def ORTHOCLASE = Stone("orthoclase", "rgb(180,110,90)")
	def BASALT     = Stone("basalt",     "#333")
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
