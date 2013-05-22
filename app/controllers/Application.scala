package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._

object Application extends Controller {

	def index = Action {
		Ok(views.html.index("tiles2"))
	}

	def ws = WebSocket.using[String] { request =>
		val in = Iteratee.foreach[String](println)

		val out = Enumerator("{'message': 'pong'}");

		(in, out)
	}

}
