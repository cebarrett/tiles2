package models

/**
 * The material out of which an Item is crafted.
 * Items crafted of a Material will inherit the material's color,
 * and probably other properties in the future (such as tool strength).
 */
abstract class Material(val color:String) {
	def kind:String = getClass toString() toLowerCase()
}

case object Wood extends Material("brown")

abstract class Stone(override val color:String) extends Material(color)
case object Chalk extends Stone("#bbb")
case object Limestone extends Stone("#999")
case object Granite extends Stone("#666")
case object Basalt extends Stone("#444")

abstract class Metal(override val color:String) extends Material(color)
case object Copper extends Metal("sienna")
case object Iron extends Metal("#7C7060")
case object Silver extends Metal("silver")
case object Gold extends Metal("gold")
