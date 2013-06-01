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
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsPath.readNullable
import play.api.libs.json.JsPath.writeNullable
import play.api.libs.json.Writes
import play.api.libs.json.Writes.arrayWrites
import play.api.libs.json.Writes.traversableWrites

// FIXME: this class is getting too big

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
		}

		case Talk(playerName:String, message:JsValue) => {
			Logger.debug(s"Received message from $playerName: $message")
			val id:String = (message \ "id").asOpt[String].getOrElse(null)
			id match {
				case "init" =>
					Logger.info("Init new player: " + playerName)
					playerChannels.get(playerName).map({
						_.push(JsObject(Seq(
							"id" -> JsString("spawn"),
							"chunks" -> Json.toJson(Seq(
								world.chunk(0,0),
								world.chunk(0,1),
								world.chunk(1,0),
								world.chunk(1,1)
							)),
							"player" -> JsObject(Seq(
								"name" -> JsString(playerName),
								"inventory" -> Json.toJson(Seq(
									JsObject(Seq(
										"id" -> JsString("Club")
									)),
									JsObject(Seq(
										"id" -> JsString("Banana"),
										"count" -> JsNumber(10)
									))
								)),
								"x" -> JsNumber(16),
								"y" -> JsNumber(16)
							))
						)))
					})
				case _ =>
					Logger.warn("unknown message id: " + id)
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
	implicit val writesEntity = new Writes[Entity] {
		def writes(t:Entity):JsValue = JsObject(Seq("id" -> JsString(t.id)))
	}
	implicit val writesPlayerEntity = Json.writes[PlayerEntity]
	implicit val writesOptionEntity = new Writes[Option[Entity]] {
		// FIXME: this is not including PlayerEntity's name attribute
		def writes(t:Option[Entity]):JsValue = if (t.isDefined) Json.toJson(t) else JsNull
	}
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	implicit val writesWorldEvent = Json.writes[WorldEvent]
}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
