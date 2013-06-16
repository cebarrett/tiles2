package test

import org.junit.Assert._
import org.junit.Test
import models._

class TestCoordinates {

	val world:World = new World()
	
	@Test
	def testNegativeXY():Unit = {
		val pos = WorldCoordinates(-1, -1)
		val cc = pos.toChunkCoordinates
		assertEquals(-1, cc.cx)
		assertEquals(-1, cc.cy)
		val tc = pos.toTileCoordinates
		assertEquals(15, tc.tx)
		assertEquals(15, tc.ty)
		val samePos = tc.toWorldCoordinates(cc)
		assertEquals(pos.x, samePos.x)
		assertEquals(pos.y, samePos.y)
	}
}
