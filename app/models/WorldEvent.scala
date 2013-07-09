package models

import play.api.{Logger => log}

// XXX: should be several subclasses, not this monstrosity
case class WorldEvent(
	val time:String,
	val kind:String, // TODO: deprecate, then remove
	val x:Option[Int] = None,
	val y:Option[Int] = None,
	val tile:Option[Tile] = None,
	val player:Option[Player] = None,
	val prevX:Option[Int] = None,
	val prevY:Option[Int] = None
) {
	def pos:Option[WorldCoordinates] = {
		if (x.isDefined && y.isDefined) Some(WorldCoordinates(x.get,y.get))
		else None
	}
}
