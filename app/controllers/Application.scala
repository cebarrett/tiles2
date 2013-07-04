package controllers

import scala.Option.option2Iterable
import scala.concurrent.duration._

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import models.CannotConnect
import models.Connected
import models.Game
import models.Join
import models.Quit
import models.Talk
import models.Loop
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsNull
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket

object Application extends Controller {
	
	private implicit val akkaTimeout = akka.util.Timeout(1 second)
	
	private val game = Akka.system.actorOf(Props[Game])
	// XXX: should be done in game and not using akka
	private val loop = {
		Akka.system.scheduler.schedule(
			.25 seconds,
			.25 seconds,
			game,
			Loop()
		)
	}
	
	private var hitCounter:Int = 0

	def index = Action { request =>
		hitCounter = hitCounter + 1
		// use player name in the url, the session, or generate one
		Ok(views.html.index("tiles2")).withSession(
			"playerName" -> request.queryString.get("player").getOrElse(Seq.empty).map({_.trim}).filter({!_.isEmpty}).headOption.getOrElse {
				request.session get "playerName" getOrElse {
					s"player $hitCounter"
				}
			}
		)
	}

	def ws = WebSocket.async[JsValue] { request =>
		val playerName:String = request.session.get("playerName").head
		connectPlayer(playerName)
	}
	
	/**
	 * Join the world as the player with the given name.
	 * Returns an iteratee that consumes the player's
	 * messages and an enumerator that pushes messages
	 * to the player.
	 */
	def connectPlayer(playerName:String):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
		(game ? Join(playerName)).map {
			case Connected(enumerator) => 
				val iteratee = Iteratee.foreach[JsValue] { message =>
					game ! Talk(playerName, message)
				}.mapDone { _ =>
					game ! Quit(playerName)
				}
				(iteratee, enumerator)
			case CannotConnect(error) =>
				Logger warn s"Could not connect player ${playerName}: $error"
				val iteratee = Done[JsValue,Unit]((),Input.EOF)
				val enumerator = Enumerator[JsValue](JsNull).andThen(Enumerator.enumInput(Input.EOF))
				(iteratee,enumerator)
		}
	}

}

