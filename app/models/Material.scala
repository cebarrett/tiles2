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
case object Sandstone extends Stone(0.30, 0.30, "rgb(105, 98, 83)")
case object Mudstone  extends Stone(0.30, 0.30, "#52503F")
case object Limestone extends Stone(0.30, 0.35, "#5c5c55")
case object Phyllite  extends Stone(0.40, 0.35, "#606060")
case object Slate     extends Stone(0.40, 0.35, "#606063")
case object Granite   extends Stone(0.40, 0.40, "#5c5555")
case object Quartzite extends Stone(0.30, 0.40, "#7C7472")
case object Basalt    extends Stone(0.40, 0.45, "#414140")
case object Gabbro    extends Stone(0.40, 0.45, "#505050")
case object Diorite   extends Stone(0.40, 0.50, "#888888")
case object Obsidian  extends Stone(0.30, 0.50, "#1C1A1E")

abstract class Ore(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Malachite   extends Ore(0.40, 0.50, "rgb(53, 71, 64)")
case object Hematite    extends Ore(0.40, 0.50, "rgb(78, 53, 47)")
case object Cassiterite extends Ore(0.40, 0.50, "rgb(66, 61, 49)")
case object Sphalerite  extends Ore(0.40, 0.50, "rgb(60, 75, 50)")
case object Ilmenite    extends Ore(0.40, 0.50, "rgb(50, 45, 40)")
case object Galena      extends Ore(0.40, 0.50, "rgb(60, 60, 65)")

abstract class Metal(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Copper    extends Metal(0.60, 0.60, "rgb(120, 60, 35)")
case object Iron      extends Metal(0.70, 0.80, "#B1B1B0")
case object Silver    extends Metal(0.90, 0.30, "#BABBBB")
case object Gold      extends Metal(1.00, 0.30, "rgb(207, 183, 0)")
case object Tin       extends Metal(0.50, 0.50, "#8A8A8A")
case object Lead      extends Metal(0.50, 0.50, "#8F8F8F")
case object Bronze    extends Metal(0.70, 0.70, "rgb(168, 142, 101)")
case object Electrum  extends Metal(0.95, 0.30, "#C8C888")
case object Germanium extends Metal(0.40, 0.20, "#CDCDCD")
case object Steel     extends Metal(0.40, 0.90, "#B9B9B7")
case object Titanium  extends Metal(0.30, 0.90, "#A8A7A6")
case object Zinc      extends Metal(0.50, 0.40, "#949390")
case object Brass     extends Metal(0.50, 0.40, "#A72")
case object Platinum  extends Metal(0.95, 0.35, "#C2C2C4")

abstract class Gem(override val weight:Double, override val hardness:Double, override val color:String) extends Material
case object Diamond extends Gem(1.00, 1.00, "rgba(180, 255, 255, 0.66)")
