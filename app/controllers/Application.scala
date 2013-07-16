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
import play.api.{Logger => log}
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
import akka.actor.ActorRef
import java.util.concurrent.atomic.AtomicInteger

object Application extends Controller {
	
	private implicit val akkaTimeout = akka.util.Timeout(1 second)

	private val game:ActorRef = Akka.system.actorOf(Props[Game])
	// XXX: game loop should be done in game and not using akka
	val gameLoopStart = (15 seconds)
	val gameLoopInterval = (.25 seconds)
	log info s"Starting game loop in $gameLoopStart"
	Akka.system.scheduler.schedule(gameLoopStart, gameLoopInterval, game, Loop())
	
	private val hitCounter:AtomicInteger = new AtomicInteger(0)

	def index = Action { request =>
		hitCounter.getAndIncrement()
		// use player name in the url, the session, or generate one
		Ok(views.html.index("tiles2")).withSession(
			"playerName" -> request.queryString.get("player").getOrElse(Seq.empty).map({_.trim}).filter({!_.isEmpty}).headOption.getOrElse {
				request.session get "playerName" getOrElse {
					s"player ${hitCounter.get}"
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
				log info s"Player joined: $playerName"
				val iteratee = Iteratee.foreach[JsValue] { message =>
					game ! Talk(playerName, message)
				}.mapDone { _ =>
					game ! Quit(playerName)
				}
				(iteratee, enumerator)
			case CannotConnect(error) =>
				log warn s"Could not connect player ${playerName}: $error"
				val iteratee = Done[JsValue,Unit]((),Input.EOF)
				val enumerator = Enumerator[JsValue](JsNull).andThen(Enumerator.enumInput(Input.EOF))
				(iteratee,enumerator)
		}
	}

}

