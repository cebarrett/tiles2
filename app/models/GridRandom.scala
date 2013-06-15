package models

import play.api.Logger

/**
 * Selects random elements from a list of N for each point
 * on a grid, using sqrt(N) GridNoise generators.
 */
class GridRandom[T](val list:Seq[T], val scale:Double = 1.0) {

	val noiseGen:Seq[GridNoise] = {
		Logger.info(""+Math.log(list.length))
		val count:Int = Math.ceil(Math.log(list.length)/Math.log(2)).toInt
		0 until count map { i =>
			new GridNoise(scale)
		}
	}

	def pick(x:Int, y:Int):Option[T] = {
		if (list.length == 0) {
			return None
		}

		var len = noiseGen.length

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
