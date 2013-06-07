package models;

import play.api.libs.json._
import play.api.Logger

/**
 * Implicits to help with JSON formatting.
 */
object JsonFormatters {
	implicit val writesMaterial = Json.writes[Material]
	implicit val writesTerrain = Json.writes[Terrain]
	implicit val writesPlayerEntity:Writes[EntityPlayer] = Json.writes[EntityPlayer]
	implicit val writesTreeEntity:Writes[EntityTree] = Json.writes[EntityTree]
	implicit val writesWorkbenchEntity:Writes[EntityWorkbench] = Json.writes[EntityWorkbench]
	implicit val writesWoodEntity:Writes[EntityWood] = Json.writes[EntityWood]
	implicit val writesSaplingEntity:Writes[EntitySapling] = Json.writes[EntitySapling]
	implicit val writesLlamaEntity:Writes[EntityLlama] = Json.writes[EntityLlama]
	implicit val writesStoneEntity:Writes[EntityStone] = Json.writes[EntityStone]
	implicit val writesFurnaceEntity:Writes[EntityFurnace] = Json.writes[EntityFurnace]
	implicit val writesSawmillEntity:Writes[EntitySawmill] = Json.writes[EntitySawmill]
	implicit val writesStonecutterEntity:Writes[EntityStonecutter] = Json.writes[EntityStonecutter]
	implicit val writesEntity = new Writes[Entity] {
		def writes(t:Entity):JsValue = t match {
			case _:EntityPlayer => writesPlayerEntity.writes(t.asInstanceOf[EntityPlayer])
			case _:EntityTree   => writesTreeEntity.writes(t.asInstanceOf[EntityTree])
			case _:EntityWorkbench =>  writesWorkbenchEntity.writes(t.asInstanceOf[EntityWorkbench])
			case _:EntityWood =>  writesWoodEntity.writes(t.asInstanceOf[EntityWood])
			case _:EntitySapling => writesSaplingEntity.writes(t.asInstanceOf[EntitySapling])
			case _:EntityLlama => writesLlamaEntity.writes(t.asInstanceOf[EntityLlama])
			case _:EntityStone => writesStoneEntity.writes(t.asInstanceOf[EntityStone])
			case _:EntityFurnace => writesFurnaceEntity.writes(t.asInstanceOf[EntityFurnace])
			case _:EntitySawmill => writesSawmillEntity.writes(t.asInstanceOf[EntitySawmill])
			case _:EntityStonecutter => writesStonecutterEntity.writes(t.asInstanceOf[EntityStonecutter])
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
