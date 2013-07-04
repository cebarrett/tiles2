package models

trait Material {
	def kind:String = getClass getSimpleName() toLowerCase() replaceAll("\\$*$", "")
	/** relative scale, 0.01 = wool, 0.50 = granite, 1.00 = gold */
	def weight:Double
	/** relative scale, 0.01 = wool, 0.50 = granite, 1.00 = diamond */
	def hardness:Double
	def color:String
}

/**
 * The material out of which an Item is crafted.
 * Items crafted of a Material will inherit the material's color,
 * and probably other properties in the future (such as tool strength).
 */
abstract class AbstractMaterial(val weight:Double, val hardness:Double, val color:String) extends Material

case object Wood     extends AbstractMaterial(0.33, 0.20, "#A5753E")
case object Wool     extends AbstractMaterial(0.01, 0.01, "#DEDFDE")
case object Charcoal extends AbstractMaterial(0.15, 0.10, "#282725")

abstract class Stone(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Sandstone   extends Stone(1.00, 0.40, "rgb(105, 98, 83)")
case object Limestone   extends Stone(1.00, 0.40, "#5c5c55")
case object Granite     extends Stone(1.00, 0.50, "#5c5555")
case object Basalt      extends Stone(1.00, 0.60, "#414140")
case object Malachite   extends Stone(1.00, 0.50, "#2F6350")
case object Hematite    extends Stone(1.00, 0.50, "rgb(78, 53, 47)")
case object Cassiterite extends Stone(1.00, 0.50, "rgb(66, 61, 42)")
case object Obsidian    extends Stone(1.00, 0.70, "#1C1A1E")

abstract class Metal(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Copper   extends Metal(0.60, 0.60, "rgb(128, 53, 4)")
case object Iron     extends Metal(0.80, 0.80, "#B1B1B0")
case object Silver   extends Metal(0.90, 0.30, "#BABBBB")
case object Gold     extends Metal(1.00, 0.25, "#DDD000")
case object Tin      extends Metal(0.50, 0.60, "#8B8B8B")
case object Bronze   extends Metal(0.70, 0.70, "rgb(180, 155, 87)")
case object Electrum extends Metal(0.95, 0.35, "#EEE898")

abstract class Gem(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Diamond extends Gem(1.00, 1.00, "rgba(100, 230, 230, 0.66)")
