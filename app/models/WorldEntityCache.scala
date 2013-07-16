package models

import play.api.{Logger => log}
import scala.collection.concurrent.{TrieMap => MutableMap}

class WorldEntityCache {
	
	// map of world entities to their positions
	private val mainMap = new MutableMap[WorldEntity, WorldCoordinates]
	
	// secondary maps to help quickly look up some keys
	private val entityMap = new MutableMap[Entity, WorldEntity]
	private val playerMap = new MutableMap[Player, WorldEntity]
	private val monsterMap = new MutableMap[EntityMonster, WorldEntity]
	
	def all = mainMap.keys
	def entities = entityMap.keys
	def players = playerMap.keys
	def monsters = monsterMap.keys
	
	def put[T <: Entity](worldEntity:WorldEntity, pos:WorldCoordinates):Unit = {
		mainMap += ((worldEntity, pos))
		entityMap.put(worldEntity.entity, worldEntity)
		worldEntity.entity match {
			case entity:EntityPlayer => {
				playerMap.put(entity.player, worldEntity.asInstanceOf[WorldEntity])
			}
			case entity:EntityMonster => {
				monsterMap.put(entity, worldEntity.asInstanceOf[WorldEntity])
			}
			case _ => Unit
		}
	}
	
	def remove[T <: Entity](worldEntity:WorldEntity):Unit = {
		mainMap -= worldEntity
		entityMap.remove(worldEntity.entity)
		worldEntity.entity match {
			case entity:EntityPlayer => playerMap.remove(entity.player)
			case entity:EntityMonster => monsterMap.remove(entity)
			case _ => Unit
		}
	}
	
	def get[T <: Entity](worldEntity:WorldEntity):Option[WorldCoordinates] = {
		mainMap.get(worldEntity)
	}
	
	def get[T <: Entity](entity:T):Option[(WorldEntity, WorldCoordinates)] = {
		entityMap get entity map { worldEntity =>
			mainMap get worldEntity map { pos =>
				(worldEntity.asInstanceOf[WorldEntity], pos)
			} getOrElse null
		}
	}
	
	def get(player:Player):Option[(WorldEntity, WorldCoordinates)] = {
		playerMap get player map { worldEntity =>
			mainMap get worldEntity map { pos =>
				(worldEntity.asInstanceOf[WorldEntity], pos)
			} getOrElse null
		}
	}
	
}
