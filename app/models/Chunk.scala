package models

class Chunk (val cx:Int, val cy:Int, val tiles:Array[Array[Tile]] = new Array(math.pow(Chunk.length,2).toInt)) {

}

object Chunk {
	val length:Int = 16
	def coord(n:Int) = math.floor(n/16).toInt
}