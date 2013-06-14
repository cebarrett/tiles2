package models

import play.api.Logger

/**
 * Selects random elements from a list of N for each point
 * on a grid, using sqrt(N) GridNoise generators.
 */
class GridRandom[T](val list:Seq[T], val scale:Float = 1.0f) {

	val noiseGen:Seq[GridNoise] = {
		val count:Int = Math.ceil(Math.sqrt(list.length)).toInt
		0 until count map { i =>
			new GridNoise(scale)
		}
	}

	def pick(x:Int, y:Int):Option[T] = {
		if (list.length == 0) {
			return None
		}

		var index:Int = 0
		do {
			(0 until noiseGen.length) map { i =>
				val noise:Double = noiseGen(i).noiseAt(x, y)
				if (noise > 0) {
					index = index + Math.pow(2, i).toInt
				}
			}
		} while (index >= list.length);

		return Some(list(index))
	}
}
