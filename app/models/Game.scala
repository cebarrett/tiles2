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
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.Writes.arrayWrites
import play.api.libs.json.Writes.traversableWrites

// FIXME: this class is getting too big

class Game extends Actor {

	val world = new World
	// FIXME: game actor shouldn't communicate in JSON
	private var playerChannels = Map.empty[String, Channel[JsValue]]
	private val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

/*
 * JSON formatters
 */
implicit val terrainWrites = Json.writes[Terrain]
implicit val entityWrites = Json.writes[Entity]
implicit val tileWrites = new Writes[Tile] {
	def writes(t:Tile):JsValue = {
		var obj = JsObject(Seq(
			"terrain" -> terrainWrites.writes(t.terrain),
			"tx" -> JsNumber(t.tx),
			"ty" -> JsNumber(t.ty)
		))
		if (t.entity != null) {
			obj = obj + ("entity" -> entityWrites.writes(t.entity))
		}
		obj
	}
}
implicit val chunkWrites = Json.writes[Chunk]

	def receive = {
		case Join(playerName:String) => {
			val (playerEnumerator, playerChannel) = Concurrent.broadcast[JsValue]
			playerChannels = playerChannels.+((playerName, playerChannel))
			sender ! Connected(chatEnumerator >- playerEnumerator)
			notifyAll("playerJoin", playerName)
		}

		case Talk(playerName:String, message:JsValue) => {
			// TODO: implement
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
			// TODO: implement
			notifyAll("playerQuit", playerName)
			playerChannels = playerChannels - playerName
		}
	}
	
	def notifyAll(id:String, player:String) {
		val message = JsObject(
			Seq(
				"id" -> JsString(id),
				"player" -> JsString(player)
			)
		)
		chatChannel.push(message)
	}
}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
