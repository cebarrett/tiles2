package models

import scala.math.BigDecimal.int2bigDecimal

import akka.actor.Actor
import akka.actor.actorRef2Scala
import play.api.Logger
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsArray
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsPath.readNullable
import play.api.libs.json.JsPath.writeNullable
import play.api.libs.json.Writes
import play.api.libs.json.Writes.arrayWrites
import play.api.libs.json.Writes.traversableWrites

class Game extends Actor {

	/** The world */
	val world = new World

	/** Translates WorldEvents into JSON that can be broadcast to players. */
	val jsonWorldEventEnumerator:Enumerator[JsValue] = world.eventEnumerator.map[JsValue] { worldEvent =>
		Json.toJson(worldEvent)
	}

	/** Broadcast JSON messages to individual players. */
	private var playerChannels = Map.empty[String, Channel[JsValue]]

	/** Broadcast JSON messages to all players. */
	private val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

	def receive = {
		case Join(playerName:String) => {
			val player = world.spawnPlayer(playerName)
			val (playerEnumerator, playerChannel) = Concurrent.broadcast[JsValue]
			playerChannels = playerChannels + (playerName -> playerChannel)
			sender ! Connected(jsonWorldEventEnumerator >- chatEnumerator >- playerEnumerator)
			playerChannel.push(JsObject(Seq(
				"kind" -> JsString("spawn"),
				"chunks" -> Json.toJson(Seq(
					world.chunk(0,0),
					world.chunk(0,1),
					world.chunk(1,0),
					world.chunk(1,1)
				)),
				"player" -> Json.toJson(player)
			)))
		}

		case Talk(playerName:String, message:JsValue) => {
			Logger.debug(s"Received message from $playerName: $message")
			val kind:String = (message \ "kind").asOpt[String].getOrElse(null)
			kind match {
				case "north" =>
					world.movePlayer(playerName,  0,  1)
				case "south" =>
					world.movePlayer(playerName,  0, -1)
				case "east"  =>
					world.movePlayer(playerName,  1,  0)
				case "west"  =>
					world.movePlayer(playerName, -1,  0)
				case _ =>
					Logger.warn("unknown kind of message: " + kind)
			}
		}

		case Quit(playerName:String) => {
			val player = world.despawnPlayer(playerName)
			playerChannels = playerChannels - playerName
		}
	}

	/*
	 * JSON formatters
	 */
	implicit val writesTerrain = Json.writes[Terrain]
	implicit val writesPlayerEntity:Writes[EntityPlayer] = Json.writes[EntityPlayer]
	implicit val writesTreeEntity:Writes[EntityTree] = Json.writes[EntityTree]
	implicit val writesEntity = new Writes[Entity] {
		def writes(t:Entity):JsValue = t match {
			case _:EntityPlayer => writesPlayerEntity.writes(t.asInstanceOf[EntityPlayer])
			case _:EntityTree   => writesTreeEntity.writes(t.asInstanceOf[EntityTree])
			case _ => JsUndefined("Unknown entity class: " + t.getClass)
		}
	}
	implicit val writesOptionEntity = new Writes[Option[Entity]] {
		// FIXME: this is not including PlayerEntity's name attribute
		def writes(t:Option[Entity]):JsValue = {
			if (t.isDefined) {
				// Json.toJson(t.head)
				writesEntity.writes(t.head)
			} else {
				JsNull
			}
		}
	}
	implicit val writesItem = Json.writes[Item]
	implicit val writesInventory = new Writes[Inventory] {
		def writes(t:Inventory):JsValue = {
			JsArray(t.items.map({Json.toJson(_)}))
		}
	}
	implicit val writesPlayer = Json.writes[Player]
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	implicit val writesWorldEvent = Json.writes[WorldEvent]
}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
