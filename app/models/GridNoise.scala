package models

import scala.util.Random

/**
 * Generates a 2d noise field.
 * 
 * FIXME: has a artifact where noise at area near origin is always 0 regardless of z
 * can fix by replacing perlin noise impl with a better one?
 */
class GridNoise(val scale:Double = 1) {

	private val z:Double = GridNoise.nextZ

	private val baseScale:Double = .005 * (if (Game.DEV) 10.0 else 1)
	private val baseOffsetX = 1925; // may have bugs
	private val baseOffsetY = 2000; // may have bugs

	def noiseAt(x:Int, y:Int):Double = {
		SimplexNoise.noise(
				((x+baseOffsetX)*((1/scale)*baseScale)),
				((y+baseOffsetY)*((1/scale)*baseScale)),
				z
		).toDouble
	}
}

object GridNoise {
	private var z:Double = 2.58
	private def nextZ():Double = {
		z = z + 1
		z
	}
}