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
import play.api.libs.json._
import play.api.libs.json.JsPath.readNullable
import play.api.libs.json.JsPath.writeNullable
import play.api.libs.json.Writes
import play.api.libs.json.Writes.arrayWrites
import play.api.libs.json.Writes.traversableWrites

class Game extends Actor {

	/** The world */
	val world = new World

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
					"chunk" -> Json.toJson(world.chunk(cc.cx, cc.cy))
				)))
			}
		}
	}

	def receive = {
		case Join(playerName:String) => {
			val player = world.spawnPlayer(playerName)
			val (playerEnumerator, playerChannel) = Concurrent.broadcast[JsValue]
			playerChannels = playerChannels + (playerName -> playerChannel)
			sender ! Connected(jsonWorldEventEnumerator >- chatEnumerator >- playerEnumerator)
			sendChunks(player, None)
			playerChannel.push(JsObject(Seq(
				"kind" -> JsString("spawn"),
				"player" -> Json.toJson(player)
			)))
		}

		case Talk(playerName:String, message:JsValue) => {
			Logger.debug(s"Received message from $playerName: $message")
			val kind:String = (message \ "kind").as[String]
			kind match {
				// FIXME: players can move while a gui is open.
				// need to set a flag on the Player and unset when they select something.
				case "north" =>
					world.movePlayer(playerName,  0,  1)
					// playerChannels.get(playerName) map { playerChannel =>
					// 	playerChannel.push(JsObject(Seq(
					// 		"kind" -> JsString("chunk"),
					// 		"chunk" -> Json.toJson(world.chunk(0,2))
					// 	)))
					// }
				case "south" =>
					world.movePlayer(playerName,  0, -1)
				case "east"  =>
					world.movePlayer(playerName,  1,  0)
				case "west"  =>
					world.movePlayer(playerName, -1,  0)
				case "guiSelect" => {
					val index:Int = (message \ "index").as[Int]
					index match {
						// FIXME: this is duplicated from doEntityInteraction, make the code DRYer
						// FIXME: Select from a case object list of valid recipes instead of creating them here
						case 0 => Unit // close button
						case 1 => world.doPlayerCrafting(playerName, WorkbenchRecipe(Item("wood", Some(4)), Seq(Item("log", Some(1)))))
						case 2 => world.doPlayerCrafting(playerName, WorkbenchRecipe(Item("wooden axe", Some(1)), Seq(Item("stick", Some(1)), Item("wood", Some(1)))))
						case _ =>
							Logger.warn(s"Unknown gui index: $index");
					}
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
			val player = world.despawnPlayer(playerName)
			playerChannels = playerChannels - playerName
		}

		case Loop() => {
			world.tick()
		}
	}

	/*
	 * JSON formatters
	 *
	 * FIXME: every subclass of Entity needs its own formatter and must be
	 * added to a case of writesEntity. this is annoying.
	 */
	implicit val writesTerrain = Json.writes[Terrain]
	implicit val writesPlayerEntity:Writes[EntityPlayer] = Json.writes[EntityPlayer]
	implicit val writesTreeEntity:Writes[EntityTree] = Json.writes[EntityTree]
	implicit val writesWorkbenchEntity:Writes[EntityWorkbench] = Json.writes[EntityWorkbench]
	implicit val writesWoodEntity:Writes[EntityWood] = Json.writes[EntityWood]
	implicit val writesSaplingEntity:Writes[EntitySapling] = Json.writes[EntitySapling]
	implicit val writesLlamaEntity:Writes[EntityLlama] = Json.writes[EntityLlama]
	implicit val writesEntity = new Writes[Entity] {
		def writes(t:Entity):JsValue = t match {
			case _:EntityPlayer => writesPlayerEntity.writes(t.asInstanceOf[EntityPlayer])
			case _:EntityTree   => writesTreeEntity.writes(t.asInstanceOf[EntityTree])
			case _:EntityWorkbench =>  writesWorkbenchEntity.writes(t.asInstanceOf[EntityWorkbench])
			case _:EntityWood =>  writesWoodEntity.writes(t.asInstanceOf[EntityWood])
			case _:EntitySapling => writesSaplingEntity.writes(t.asInstanceOf[EntitySapling])
			case _:EntityLlama => writesLlamaEntity.writes(t.asInstanceOf[EntityLlama])
			case _ => {
				val msg = "writesEntity: Unknown entity class: " + t.getClass
				Logger.warn(msg)
				JsUndefined(msg)
			}
		}
	}
	implicit val writesOptionEntity = new Writes[Option[Entity]] {
		def writes(t:Option[Entity]):JsValue = {
			if (t.isDefined) {
				writesEntity.writes(t.head)
			} else {
				JsNull
			}
		}
	}
	implicit val writesItem = Json.writes[Item]
	implicit val writesInventory = new Writes[Inventory] {
		def writes(t:Inventory):JsValue = JsObject(Seq(
			"items" -> JsArray(t.items.map({Json.toJson(_)})),
			"selected" -> Json.toJson(t.selected)
		))
	}
	implicit val writesPlayer = Json.writes[Player]
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	implicit val writesWorldEvent = Json.writes[WorldEvent]
}

case class Join(playerName: String)
case class Talk(playerName: String, message:JsValue)
case class Quit(playerName: String)
case class Loop()
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)
