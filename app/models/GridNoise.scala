package models

import scala.util.Random

/**
 * FIXME: replace perlin noise impl with one that can be seeded.
 * this one generates only about a thousand unique noise fields.
 */
class GridNoise(val scale:Double = 1) {

	private val z:Int = GridNoise.nextZ

	// XXX: increasing this value hoses the server for some reason
	private val baseScale:Double = .01 * (if (Game.DEV) 5.0 else 1.0)
	private val baseOffsetX =  449;
	private val baseOffsetY = -353;

	def noiseAt(x:Int, y:Int):Double = {
		PerlinNoise.perlinNoise(((x+baseOffsetX)*scale*baseScale).toFloat, ((y+baseOffsetY)*scale*baseScale).toFloat, z).toDouble
	}
}

object GridNoise {
	private var z:Int = 839;
	private def nextZ():Int = {
		z = z + 89
		z
	}
}