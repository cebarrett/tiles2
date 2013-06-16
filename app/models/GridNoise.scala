package models

import scala.util.Random

/**
 * FIXME: replace perlin noise impl with one that can be seeded.
 * this one generates only about a thousand unique noise fields.
 */
class GridNoise(val scale:Double = 1) {

	private val z:Int = GridNoise.nextZ

	private val baseScale:Double = .005 * (if (Game.DEV) 10.0 else 1.0)
	private val baseOffsetX = 0; // may have bugs
	private val baseOffsetY = 0; // may have bugs

	def noiseAt(x:Int, y:Int):Double = {
		PerlinNoise.perlinNoise(
				((x+baseOffsetX)*((1/scale)*baseScale)).toFloat,
				((y+baseOffsetY)*((1/scale)*baseScale)).toFloat,
				z.toFloat
		).toDouble
	}
}

object GridNoise {
	private var z:Int = 7;
	private def nextZ():Int = {
		z = z + 89
		z
	}
}