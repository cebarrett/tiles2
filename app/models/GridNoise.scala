package models

import scala.util.Random

/**
 * Generates a 2d noise field.
 * 
 * FIXME: has a artifact where noise at area near origin is always 0 regardless of z
 * can fix by replacing perlin noise impl with a better one?
 */
class GridNoise(val scale:Double = 1) {

	private val z:Int = GridNoise.nextZ

	private val baseScale:Double = .005 * (if (Game.DEV) 10.0 else 1.0)
	private val baseOffsetX = 1000; // may have bugs
	private val baseOffsetY = 1000; // may have bugs

	def noiseAt(x:Int, y:Int):Double = {
		PerlinNoise.perlinNoise(
				((x+baseOffsetX)*((1/scale)*baseScale)).toFloat,
				((y+baseOffsetY)*((1/scale)*baseScale)).toFloat,
				z.toFloat
		).toDouble
	}
}

object GridNoise {
	private var z:Int = 362;
	private def nextZ():Int = {
		z = z + 113
		z
	}
}