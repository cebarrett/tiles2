package models

import scala.math.BigDecimal.int2bigDecimal
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.duration._
import scala.collection._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.actorRef2Scala
import play.api.Logger
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.json._
import play.api.libs.json.JsPath.readNullable
import play.api.libs.json.JsPath.writeNullable
import play.api.libs.json.Writes
import play.api.libs.json.Writes.arrayWrites
import play.api.libs.json.Writes.traversableWrites
import models.JsonFormatters._

object Game {
	/** set some stuff to help debug/test the game.
	    changes gameplay, so must be false for production. */
	def DEV:Boolean = false
}

/**
 * The game is an Akka actor that simulates a World, runs a game
 * loop every tick, and sends and receives messages from other
 * actors (such as the Play web controller).
 * 
 * Messages are currently instances of play.api.lib.json.JsValue
 * where information is represented in JSON format. Messages
 * can be broadcast to all players or sent to a specific player.
 */
class Game extends Actor {

	/** The world */
	val world = {
		val w = new World()
		//w.loadAllChunks		// XXX: possible performance issues & spams browser with all events
		w
	}

	/** Translates WorldEvents into JSON that can be broadcast to players,
	    and also listens for and handles certain WorldEvents. */
	val jsonWorldEventEnumerator:Enumerator[JsValue] = world.eventEnumerator.map[JsValue] { worldEvent =>
		(worldEvent.kind, worldEvent.player.isDefined) match {
			case ("entityMove", true) => {
				// a player moved so do chunk un/loading
				var oldPos = WorldCoordinates(worldEvent.prevX.get, worldEvent.prevY.get)
				var newPos = WorldCoordinates(worldEvent.x.get, worldEvent.y.get)
				if (oldPos.inSameChunk(newPos) == false) {
					sendChunks(worldEvent.player.get, Some(oldPos))
				}
			}
			case (_, _) => Unit
		}
		Json.toJson(worldEvent)
	}

	/** Broadcast JSON messages to individual players. */
	private var playerChannels = Map.empty[String, Channel[JsValue]]

	/** Broadcast JSON messages to all players. */
	private val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

	/** Schedule the game loop to run repeatedly. */
	private val gameLoop = {
		Akka.system.scheduler.schedule(
			1 seconds,
			1 seconds,
			self,
			Loop()
		)
	}

	def sendChunks(player:Player, prevPos:Option[WorldCoordinates]):Unit = {
		val playerChunkRadius = 1
		val nextPos = WorldCoordinates(player.x, player.y)
		val nextCc  = nextPos.toChunkCoordinates
		val nextRad:Set[ChunkCoordinates] = Chunk.radius(nextCc, playerChunkRadius)
		val prevRad:Set[ChunkCoordinates] = {
			prevPos map { pp:WorldCoordinates =>
				Chunk.radius(pp.toChunkCoordinates, playerChunkRadius)
			} getOrElse {
				Set.empty[ChunkCoordinates]
			}
		}
		val load:Set[ChunkCoordinates] = nextRad -- prevRad
		val unload:Set[ChunkCoordinates] = prevRad -- nextRad

		playerChannels get player.name map { channel =>
			// note: client expects the unload messages to come first
			unload map { cc =>
				channel.push(JsObject(Seq(
					"kind" -> JsString("chunkUnload"),
					"cx" -> JsNumber(cc.cx),
					"cy" -> JsNumber(cc.cy)
				)))
			}
			load map { cc =>
				channel.push(JsObject(Seq(
					"kind" -> JsString("chunk"),
					"chunk" -> Json.toJson(world.chunkAt(cc.cx, cc.cy))
				)))
			}
		}
	}

	def receive = {
		case Join(playerName:String) => {
			world.connectPlayer(playerName)
			val (playerEnumerator, playerChannel) = Concurrent.broadcast[JsValue]
			playerChannels = playerChannels + (playerName -> playerChannel)
			sender ! Connected(jsonWorldEventEnumerator >- chatEnumerator >- playerEnumerator)
		}

		case Talk(playerName:String, message:JsValue) => {
			val kind:String = (message \ "kind").as[String]
			kind match {
				case "spawn" => {
					world.spawnPlayer(playerName)
					val player:Player = (world.players get playerName).get
					sendChunks(player, None)
					val response:JsValue = JsObject(Seq(
						"kind" -> JsString("spawn"),
						"player" -> Json.toJson(player),
						"crafts" -> Json.toJson(Recipe.all)))
					playerChannels.get(playerName).get.push(response)
				}
				// FIXME: players can move while a gui is open.
				// need to set a flag on the Player and unset when they select something.
				case "north" =>
					world.movePlayer(playerName,  0,  1)
				case "south" =>
					world.movePlayer(playerName,  0, -1)
				case "east"  =>
					world.movePlayer(playerName,  1,  0)
				case "west"  =>
					world.movePlayer(playerName, -1,  0)
				case "craft" => {
					val craft:String = (message \ "craft").as[String]
					val index:Int = (message \ "index").as[Int]
					world.doPlayerCrafting(playerName, craft, index)
				}
				case "selectItem" =>
					val index:Int = (message \ "index").as[Int]
					world.doSelectItem(playerName, index)
				case "place" => {
					val x:Int = (message \ "x").as[Int]
					val y:Int = (message \ "y").as[Int]
					world.doPlaceItem(playerName, WorldCoordinates(x,y))
				}
				case _ =>
					Logger.warn("unknown kind of message: " + kind)
			}
		}

		case Quit(playerName:String) => {
			world.players get playerName map { _ =>
				world.despawnPlayer(playerName)
			}
			playerChannels = playerChannels - playerName
		}

		case Loop() => {
			world.tick()
		}
	}

}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Loop()
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
