package models

/**
 * The material out of which an Item is crafted.
 * Items crafted of a Material will inherit the material's color,
 * and probably other properties in the future (such as tool strength).
 */
abstract class Material(val color:String) {
	def kind:String = getClass getSimpleName() toLowerCase() replaceAll("\\$*$", "")
}

case object Wood extends Material("#A67E35")
case object Charcoal extends Material("#202020")

abstract class Stone(override val color:String) extends Material(color)
case object Chalk extends Stone("#C0CACA")
case object Limestone extends Stone("#5c5c55")
case object Granite extends Stone("#5c5555")
case object Basalt extends Stone("#414140")

abstract class Metal(override val color:String) extends Material(color)
case object Copper extends Metal("rgb(205, 90, 35)")
case object Iron extends Metal("#C8C5C2")
case object Silver extends Metal("#C8D8D8")
case object Gold extends Metal("#DDD000")
