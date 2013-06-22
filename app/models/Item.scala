package models

trait Item {
	def kind:String
}

trait Placeable {
	def entity:Entity
}

// case object ItemSapling(val kind = "sapling", val entity = EntitySapling()) extends Item with Placeable