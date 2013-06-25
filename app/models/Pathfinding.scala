package models

import scala.collection.mutable.PriorityQueue

case class Path(val dest:WorldCoordinates) {
	def directionFrom(pos:WorldCoordinates):Direction = {
		// go in the direction of the target w/o checking for obstacles
		val (n, e, s, w) = (
			WorldCoordinates(pos.x, pos.y+1).distanceTo(dest),
			WorldCoordinates(pos.x+1, pos.y).distanceTo(dest),
			WorldCoordinates(pos.x, pos.y-1).distanceTo(dest),
			WorldCoordinates(pos.x-1, pos.y).distanceTo(dest))
		if (n < e && n < s && n < w) North
		else if (e < s && e < w) East
		else if (s < w) South
		else West
	}
}