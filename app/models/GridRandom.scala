package models

/**
 * Select a random element from a list of N for each point
 * on a grid, using sqrt(N) GridNoise generators.
 */
class GridRandom[T](val scale:Float, val list:Seq[T]) {

	val noiseGen:Seq[GridNoise] = {
		val len:Int = Math.ceil(Math.sqrt(list.length)).toInt
		0 until list.length map { i =>
			new GridNoise
		}
	}

	def pick(x:Int, y:Int, options:List[T]):Option[T] = {
		val noise = PerlinNoise.perlinNoise((x*scale).toFloat, (y*scale).toFloat, 100)
		list.headOption
	}
}
