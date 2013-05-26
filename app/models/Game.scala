package models

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import play.api._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

// FIXME: this class is getting too big

object Game {
	
	implicit val timeout = Timeout(1 second)
	
	lazy val default = {
		val roomActor = Akka.system.actorOf(Props[Game])
		
		// Create a bot user (just for fun)
		//Robot(roomActor)
		
		roomActor
	}

	def join(playerName:String):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

		(default ? Join(playerName)).map {
			
			case Connected(enumerator) => 
			
				// Create an Iteratee to consume the feed
				val iteratee = Iteratee.foreach[JsValue] { message =>
					default ! Talk(playerName, message)
				}.mapDone { _ =>
					default ! Quit(playerName)
				}

				(iteratee,enumerator)
				
			case CannotConnect(error) => 
			
				// Connection error

				// A finished Iteratee sending EOF
				val iteratee = Done[JsValue,Unit]((),Input.EOF)

				// Send an error and close the socket
				val enumerator =	Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
				
				(iteratee,enumerator)
				 
		}

	}
	
}

class Game extends Actor {

	implicit val terrainWrites = Json.writes[Terrain]
	implicit val entityWrites = Json.writes[Entity]
	implicit val tileWrites = new Writes[Tile] {
		def writes(t:Tile):JsValue = {
			var obj = Json.obj("terrain" -> terrainWrites.writes(t.terrain))
			if (t.entity != null) {
				obj = obj + ("entity" -> entityWrites.writes(t.entity))
			}
			obj
		}
	}
	implicit val chunkWrites = Json.writes[Chunk]

	private def world = new World
	private var playerChannels = Map.empty[String, Channel[JsValue]]
	val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

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
					// FIXME: push to a channel for this player
					playerChannels.get(playerName).map({
						val chunk = world.chunk(0,0)
						_.push(JsObject(Seq(
							"id" -> JsString("spawn"),
							"chunk" -> Json.toJson[Chunk](chunk)
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


