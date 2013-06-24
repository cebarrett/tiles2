package models

/**
 * The material out of which an Item is crafted.
 * Items crafted of a Material will inherit the material's color,
 * and probably other properties in the future (such as tool strength).
 */
abstract class Material(val color:String) {
	def kind:String = getClass getSimpleName() toLowerCase() replaceAll("\\$*$", "")
}

case object Wood extends Material("#AA6600")

abstract class Stone(override val color:String) extends Material(color)
case object Chalk extends Stone("#D8DADC")
case object Limestone extends Stone("#6c6c66")
case object Granite extends Stone("#5c5555")
case object Basalt extends Stone("#383030")

abstract class Metal(override val color:String) extends Material(color)
case object Copper extends Metal("#A0501A")
case object Iron extends Metal("#CCB")
case object Silver extends Metal("#CDD")
case object Gold extends Metal("#DDD000")
