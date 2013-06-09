package models;

import play.api.libs.json._
import play.api.Logger

/**
 * Implicits to help with JSON formatting.
 */
object JsonFormatters {
	// FIXME: don't write an AI at all
	implicit val writesAI = new Writes[AI] {
		def writes(arg:AI):JsValue = JsNull
	}
	implicit val writesMaterial = new Writes[Material] {
		def writes(arg:Material):JsValue = {
			JsObject(Seq(
				"kind" -> JsString(arg.kind),
				"category" -> JsString(arg.category),
				"color" -> JsString(arg.color)
			))
		}
	}
	implicit val writesTerrain = Json.writes[Terrain]
	implicit val writesPlayerEntity:Writes[EntityPlayer] = Json.writes[EntityPlayer]
	implicit val writesTreeEntity:Writes[EntityTree] = Json.writes[EntityTree]
	implicit val writesWorkbenchEntity:Writes[EntityWorkbench] = Json.writes[EntityWorkbench]
	implicit val writesSaplingEntity:Writes[EntitySapling] = Json.writes[EntitySapling]
	implicit val writesMobEntity:Writes[EntityMob] = new Writes[EntityMob] {
		def writes(t:EntityMob):JsValue = {
			JsObject(Seq(
				"id" -> JsString(t.id),
				"hitPoints" -> JsNumber(t.hitPoints)
			))
		}
	}
	implicit val writesStoneEntity:Writes[EntityStone] = Json.writes[EntityStone]
	implicit val writesOreEntity:Writes[EntityOre] = Json.writes[EntityOre]
	implicit val writesFurnaceEntity:Writes[EntityFurnace] = Json.writes[EntityFurnace]
	implicit val writesSawmillEntity:Writes[EntitySawmill] = Json.writes[EntitySawmill]
	implicit val writesStonecutterEntity:Writes[EntityStonecutter] = Json.writes[EntityStonecutter]
	implicit val writesEntity = new Writes[Entity] {
		def writes(t:Entity):JsValue = t match {
			case _:EntityPlayer => writesPlayerEntity.writes(t.asInstanceOf[EntityPlayer])
			case _:EntityTree   => writesTreeEntity.writes(t.asInstanceOf[EntityTree])
			case _:EntityWorkbench =>  writesWorkbenchEntity.writes(t.asInstanceOf[EntityWorkbench])
			case _:EntitySapling => writesSaplingEntity.writes(t.asInstanceOf[EntitySapling])
			case _:EntityStone => writesStoneEntity.writes(t.asInstanceOf[EntityStone])
			case _:EntityFurnace => writesFurnaceEntity.writes(t.asInstanceOf[EntityFurnace])
			case _:EntitySawmill => writesSawmillEntity.writes(t.asInstanceOf[EntitySawmill])
			case _:EntityStonecutter => writesStonecutterEntity.writes(t.asInstanceOf[EntityStonecutter])
			case _:EntityOre => writesOreEntity.writes(t.asInstanceOf[EntityOre])
			case entity:EntityMob => Json.toJson(entity)
			case _ => {
				val msg = "writesEntity: Unknown entity class: " + t.getClass
				Logger.error(msg)
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
