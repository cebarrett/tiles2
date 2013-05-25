package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.json._
import akka.actor._
import scala.concurrent.duration._
import models._

object Application extends Controller {

	private var hitCounter:Int = 0

	def index = Action {
		hitCounter = hitCounter + 1
		val playerName = s"player $hitCounter"
		Ok(views.html.index("tiles2")).withSession(
			"playerName" -> playerName
		)
	}

	def ws = WebSocket.async[JsValue] { request =>
		val playerName = request.session.get("playerName")
		Game.join(playerName getOrElse null)
	}

}
