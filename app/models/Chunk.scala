package models

case class Chunk (val cx:Int, val cy:Int, val tiles:Array[Array[Tile]] = Array.ofDim[Tile](Chunk.length, Chunk.length)) {

	def tile(x:Int, y:Int, tile:Tile = null):Tile = {
		if (tile != null) {
			tiles(Tile.coord(x))(Tile.coord(y)) = tile
		}
		tiles(Tile.coord(x))(Tile.coord(y))
	}

}

object Chunk {
	val length:Int = 16
	def coord(worldCoord:Int):Int = math.floor(worldCoord/length).toInt
}

case class ChunkCoordinates(val cx:Int, val cy:Int) {
	require(0 <= cx && cx < World.length && 0 <= cy && cy < World.length)
}

case class ChunkRadius(val x:Int, val y:Int, val radius:Int) {
	def difference(other:ChunkGrid):Seq[ChunkCoordinates] = {
/*
		var chunkCoords = [];
		var minX = this.get("x") - this.get("radius");
		var maxX = this.get("x") + this.get("radius");
		var minY = this.get("y") - this.get("radius");
		var maxY = this.get("y") + this.get("radius");
		for (var x = minX; x <= maxX; ++x) {
			for (var y = minY; y <= maxY; ++y) {
				if (!otherRadius.contains({x: x, y: y})) {
					chunkCoords.push({cx: x, cy: y});
				}
			}
		}
		return chunkCoords;*/
		Seq() // TODO
	}
	def contains(coords:ChunkCoordinates):Boolean = {
/*		var minX = this.get("x") - this.get("radius");
		var maxX = this.get("x") + this.get("radius");
		var minY = this.get("y") - this.get("radius");
		var maxY = this.get("y") + this.get("radius");
		return (point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY);

*/
		false	// TODO
	}
	def allChunks():Seq[ChunkCoordinates] = {
/*		var chunkCoords = [];
		var minX = this.get("x") - this.get("radius");
		var maxX = this.get("x") + this.get("radius");
		var minY = this.get("y") - this.get("radius");
		var maxY = this.get("y") + this.get("radius");
		for (var x = minX; x <= maxX; ++x) {
			for (var y = minY; y <= maxY; ++y) {
				chunkCoords.push({cx: x, cy: y});
			}
		}
		return chunkCoords;*/
		Seq() // TODO
	}
}