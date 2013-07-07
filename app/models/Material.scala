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

case object Wood     extends AbstractMaterial(0.20, 0.20, "#A5753E")
case object Wool     extends AbstractMaterial(0.01, 0.01, "#DEDFDE")
case object Charcoal extends AbstractMaterial(0.10, 0.10, "#282725")

abstract class Stone(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Sandstone   extends Stone(0.30, 0.30, "rgb(105, 98, 83)")
case object Limestone   extends Stone(0.30, 0.40, "#5c5c55")
case object Granite     extends Stone(0.40, 0.40, "#5c5555")
case object Basalt      extends Stone(0.40, 0.50, "#414140")
case object Obsidian    extends Stone(0.30, 0.99, "#1C1A1E")

abstract class Ore(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Malachite   extends Ore(0.40, 0.50, "rgb(53, 71, 64)")
case object Hematite    extends Ore(0.40, 0.50, "rgb(78, 53, 47)")
case object Cassiterite extends Ore(0.40, 0.50, "rgb(66, 61, 49)")

abstract class Metal(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Copper   extends Metal(0.60, 0.60, "rgb(128, 53, 4)")
case object Iron     extends Metal(0.80, 0.80, "#B1B1B0")
case object Silver   extends Metal(0.90, 0.40, "#BABBBB")
case object Gold     extends Metal(1.00, 0.40, "rgb(207, 183, 0)")
case object Tin      extends Metal(0.50, 0.50, "#8A8A8A")
case object Lead     extends Metal(0.50, 0.50, "#8F8F8F")
case object Bronze   extends Metal(0.70, 0.70, "rgb(168, 142, 101)")
case object Electrum extends Metal(0.95, 0.35, "#C8C888")

abstract class Gem(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Diamond extends Gem(1.00, 1.00, "rgba(130, 230, 230, 0.5)")
