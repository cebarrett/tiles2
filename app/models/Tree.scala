package models

import play.api.{Logger => log}
import util.Random.{nextInt => randInt}

class EntityTree extends Entity {
	override def defense = Wood.hardness * 1.5
	override def drops = Seq(ItemStack(new EntityBlock(Wood)), ItemStack(new EntitySapling, Some(randInt(4))))
	override def canBeBrokenBy(tool:Option[Tool]):Boolean = {
		tool map { tool =>
			tool.isInstanceOf[Axe]
		} getOrElse false
	}
}

class EntitySapling extends Entity {
	override def drops = Seq(ItemStack(this))
	override def canBeBrokenBy(tool:Option[Tool]):Boolean = {
		tool map { tool =>
			tool.isInstanceOf[Axe]
		} getOrElse false
	}
	override def tick(world:World, coords:WorldCoordinates):Unit = {
		val tile:Tile = world.tileAt(coords)
		val chanceOfTreeGrowing:Double = 0.0003;
		if (Math.random() < chanceOfTreeGrowing) {
			tile.entity = Some(new EntityTree())
			// XXX: next line seems out of place
			world.broadcastTileEvent(coords)
		}
	}
}
