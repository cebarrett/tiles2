package models

import scala.util.Random

/**
 * FIXME: replace perlin noise impl with one that can be seeded.
 * this one generates only about a thousand unique noise fields.
 */
class GridNoise(val scale:Double = 1) {

	private val z:Int = GridNoise.nextZ

	// XXX: increasing this value hoses the server for some reason
	private val baseScale:Double = .01

	def noiseAt(x:Int, y:Int):Double = {
		PerlinNoise.perlinNoise((x*scale*baseScale).toFloat, (y*scale*baseScale).toFloat, z).toDouble
	}
}

object GridNoise {
	private var z:Int = 50;
	private def nextZ():Int = {
		z = z + 100
		z
	}
}