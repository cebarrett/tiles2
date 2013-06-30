package models

/**
 * The material out of which an Item is crafted.
 * Items crafted of a Material will inherit the material's color,
 * and probably other properties in the future (such as tool strength).
 */
abstract class Material(val color:String) {
	def kind:String = getClass getSimpleName() toLowerCase() replaceAll("\\$*$", "")
}

case object Wood extends Material("#A5753E")
case object Charcoal extends Material("#282725")

abstract class Stone(override val color:String) extends Material(color)
case object Sandstone extends Stone("rgb(110, 100, 60)")
case object Limestone extends Stone("#5c5c55")
case object Granite extends Stone("#5c5555")
case object Basalt extends Stone("#414140")
case object Malachite extends Stone("#2F6350")
case object Hematite extends Stone("#662211")
case object Cassiterite extends Stone("#706620")
case object Obsidian extends Stone("#1F1D1E")

abstract class Metal(override val color:String) extends Material(color)
case object Copper extends Metal("#973B00")
case object Iron extends Metal("#B1B1B0")
case object Silver extends Metal("#BABBBB")
case object Gold extends Metal("#DDD000")
case object Tin extends Metal("#8B8B8B")
case object Bronze extends Metal("rgb(180, 155, 87)")
case object Electrum extends Metal("#EEE898")