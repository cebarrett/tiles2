package models

//class WorldEntityCache extends collection.mutable.HashMap[WorldEntity[_ <: Entity],WorldCoordinates] {
class WorldEntityCache {
	
	// map of world entities to their positions
	private val mainMap = new collection.mutable.HashMap[WorldEntity[_], WorldCoordinates]
	
	// secondary maps to help quickly look up some keys
	private val entityMap = new collection.mutable.HashMap[Entity, WorldEntity[_]]
	private val playerMap = new collection.mutable.HashMap[Player, WorldEntity[EntityPlayer]]
	
	def all = mainMap.keys
	
	def put[T <: Entity](worldEntity:WorldEntity[T], pos:WorldCoordinates):Unit = {
		mainMap.put(worldEntity, pos)
		entityMap.put(worldEntity.entity, worldEntity)
		worldEntity.entity match {
			case entity:EntityPlayer => playerMap.put(entity.player,
					worldEntity.asInstanceOf[WorldEntity[EntityPlayer]])
			case _ => Unit
		}
	}
	
	def remove[T <: Entity](worldEntity:WorldEntity[T]):Unit = {
		mainMap.remove(worldEntity)
		entityMap.remove(worldEntity.entity)
		worldEntity.entity match {
			case entity:EntityPlayer => playerMap.remove(entity.player)
			case _ => Unit
		}
	}
	
	def get[T <: Entity](worldEntity:WorldEntity[T]):Option[WorldCoordinates] = {
		mainMap.get(worldEntity)
	}
	
	def get[T <: Entity](entity:T):Option[(WorldEntity[T], WorldCoordinates)] = {
		entityMap get entity map { worldEntity =>
			mainMap get worldEntity map { pos =>
				(worldEntity.asInstanceOf[WorldEntity[T]], pos)
			} getOrElse null
		}
	}
	
	def get(player:Player):Option[(WorldEntity[EntityPlayer], WorldCoordinates)] = {
		playerMap get player map { worldEntity =>
			mainMap get worldEntity map { pos =>
				(worldEntity.asInstanceOf[WorldEntity[EntityPlayer]], pos)
			} getOrElse null
		}
	}
	
}
