package models

trait Vehicle

class Boat(override val material:Material) extends AbstractItemWithMaterial(material) with Vehicle
