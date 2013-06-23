package models

abstract class Material(val color:String)

case object Cheese extends Material("#FB1")
case object Plant extends Material("#22D")
case object Wood extends Material("brown")

abstract class Stone(val color:String) extends Material(color)
case object Chalk extends Stone("#bbb")
case object Limestone extends Stone("#999")
case object Granite extends Stone("#666")
case object Basalt extends Stone("#444")

abstract class Metal(val color:String) extends Material(color)
case object Copper extends Metal("sienna")
case object Iron extends Metal("#907868")
case object Silver extends Metal("silver")
case object Gold extends Metal("gold")
