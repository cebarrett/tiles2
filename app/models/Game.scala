package models

import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

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
	val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

	def receive = {

		case Join(playerName:String) => {
			sender ! Connected(chatEnumerator)
			// if (players.contains(playerName)) {
			// 	world.players += new Player(playerName)
			// }
			notifyAll("playerJoin", playerName)
		}

		case Talk(playerName:String, message:JsValue) => {
			// TODO: implement
		}

		case Quit(playerName:String) => {
			// TODO: implement
		}


	}
	
	def notifyAll(id:String, player:String) {
		val msg = JsObject(
			Seq(
				"id" -> JsString(id),
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
/*
private object Robot {

	def apply(chatRoom: ActorRef) {
		
		// Create an Iteratee that logs all messages to the console.
		val loggerIteratee = Iteratee.foreach[JsValue](event => Logger("robot").info(event.toString))
		
		implicit val timeout = Timeout(1 second)
		// Make the robot join the room
		chatRoom ? (Join("Robot")) map {
			case Connected(robotChannel) => 
				// Apply this Enumerator on the logger.
				robotChannel |>> loggerIteratee
		}
		
		// Make the robot talk every 30 seconds
		Akka.system.scheduler.schedule(
			30 seconds,
			30 seconds,
			chatRoom,
			Talk("Robot", "I'm still alive")
		)
	}
*/