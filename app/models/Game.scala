package models

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import play.api._
import play.api.Play.current
import play.api.libs.json._
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

		case Talk(playerName:String, data:JsValue) => {
			// TODO: implement
			Logger.debug(s"Received message from $playerName: $data")
			val message:String = (data \ "message").asOpt[String].getOrElse(null)
			message match {
				case "init" =>
					Logger.info("Init new player: " + playerName)
					// FIXME: push to a channel for this player
					playerChannels.get(playerName).map({
						_.push(JsObject(Seq("message" -> JsString("hi"))))
					})
				case _ =>
					Logger.warn("unknown message: " + message)
			}
		}

		case Quit(playerName:String) => {
			// TODO: implement
			notifyAll("playerQuit", playerName)
			playerChannels = playerChannels - playerName
		}
	}
	
	def notifyAll(message:String, player:String) {
		val msg = JsObject(
			Seq(
				"message" -> JsString(message),
				"player" -> JsString(player)
			)
		)
		chatChannel.push(msg)
	}
}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
