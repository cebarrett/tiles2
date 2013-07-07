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
import java.util.Timer
import java.util.TimerTask

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

	/** The game world */
	val world = new World
	world.loadAllChunks

	/**
	 * Translates WorldEvents into JSON that can be broadcast to players,
	 * and also listens for and handles certain WorldEvents.
	 */
	world.eventEnumerator |>>> Iteratee.foreach[WorldEvent] { worldEvent =>
		val json = Json.toJson(worldEvent)
		// check if a player moved and do chunk un/loading for that player
		(worldEvent.kind, worldEvent.player.isDefined) match {
			case ("entityMove", true) => {
				var oldPos = WorldCoordinates(worldEvent.prevX.get, worldEvent.prevY.get)
				var newPos = WorldCoordinates(worldEvent.x.get, worldEvent.y.get)
				if (oldPos.inSameChunk(newPos) == false) {
					sendChunks(worldEvent.player.get, Some(oldPos))
				}
			}
			case (_, _) => Unit
		}
		// send the event to all players within D meters
		// if the event has no position, send to everyone
		val D = 50.0
		playerChannels foreach { entry =>
			val (playerName, channel) = entry
			world.players get playerName map { player =>
				worldEvent.pos map { pos =>
					if ((pos distanceTo player.pos) < D) {
						playerChannels.get(playerName).get.push(json)
					}
				} getOrElse {
					playerChannels.get(playerName).get.push(json)
				}
			}
		}
	}

	/** Broadcast JSON messages to individual players. */
	private var playerChannels = Map.empty[String, Channel[JsValue]]

	def sendChunks(player:Player, prevPos:Option[WorldCoordinates]):Unit = {
		val playerChunkRadius = 2
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
			playerChannels get playerName map { p =>
				sender ! CannotConnect("player is already logged in")
			} getOrElse {
				world.connectPlayer(playerName)
				val (playerEnumerator, playerChannel) = Concurrent.broadcast[JsValue]
				playerChannels = playerChannels + (playerName -> playerChannel)
				sender ! Connected(playerEnumerator)
			}
		}

		case Talk(playerName:String, message:JsValue) => {
			val kind:String = (message \ "kind").as[String]
			kind match {
				case "spawn" => {
					playerChannels get playerName map { channel =>
						world.spawnPlayer(playerName)
						world.players get playerName map { player =>
							sendChunks(player, None)
							val response:JsValue = JsObject(Seq(
								"kind" -> JsString("spawn"),
								"player" -> Json.toJson(player),
								"crafts" -> Json.toJson(Recipe.all)))
							channel push response
							world.broadcastPlayer(player)
						}
					} getOrElse {
						Logger warn s"Tried to spawn $playerName but couldn't find their Channel"
					}
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
					message.\\("index").headOption.map({ index =>
						index match {
							case index:JsNumber => world.doSelectItem(playerName, index.value.toInt)
							case _ => Unit
						}
					}).getOrElse(world doDeselectItem playerName)
				case "swap" =>
					(message \\ "from").headOption map { from =>
						(message \\ "to").headOption map { to =>
							(from, to) match {
								case (JsNumber(i0), JsNumber(i1)) => {
									world.doSwapItems(playerName, i0.toInt, i1.toInt)
								}
								case _ => Unit
							}
						}
					}
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
			try {
				world.tick
			} catch {
				case t:Throwable => Logger error ("world.tick threw exception", t)
			}
		}
	}

}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Loop()
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
