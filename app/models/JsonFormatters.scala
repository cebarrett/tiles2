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
				"color" -> JsString(arg.color)
			))
		}
	}
	implicit val writesTerrain = Json.writes[Terrain]
	implicit val writesTool = new Writes[Tool] {
		def writes(tool:Tool):JsValue = JsObject(Seq(
			"kind"     -> Json.toJson(tool.kind),
			"material" -> Json.toJson(tool.material)
		))
	}
	implicit val writesItem = new Writes[Item] {
		def writes(item:Item):JsValue = item match {
			case item:EntityBlock => JsObject(Seq("kind" -> JsString(item.kind), "material" -> Json.toJson(item.material)))
			case item:Tool        => JsObject(Seq("kind" -> JsString(item.kind), "material" -> Json.toJson(item.material)))
			case _                => JsObject(Seq("kind" -> JsString(item.kind)))
		}
	}
	implicit val writesItemStack = Json.writes[ItemStack]
	implicit val writesIngredient = new Writes[Ingredient] {
		def writes(obj:Ingredient):JsValue = {
			obj match {
				case obj:IngredientMaterial[_] => JsObject(Seq("kind" -> JsString(obj.material.getSimpleName.toLowerCase), "count" -> JsNumber(obj.count)))
				case obj:IngredientItem        => JsObject(Seq("kind" -> JsString(obj.item.kind),                          "count" -> JsNumber(obj.count)))
				case _ => JsNull
			}
		}
	}
	implicit val writesRecipe = Json.writes[Recipe]
	implicit val writesAllRecipes = new Writes[Map[String,Seq[Recipe]]] {
		def writes(e:Map[String,Seq[Recipe]]):JsValue = {
			JsArray(
				e.toSeq map { obj:(String, Seq[Recipe]) =>
					val (kind:String, recipes:Seq[Recipe]) = obj;
					val jsRecipes:JsArray = JsArray(recipes map {
						Json.toJson(_)
					})
					JsObject(Seq(
						"kind" -> JsString(kind),
						"recipes" -> jsRecipes
					))
				}
			)
		}
	}
	implicit val writesInventory = new Writes[Inventory] {
		def writes(t:Inventory):JsValue = JsObject(Seq(
			"items" -> JsArray(t.items.map({Json.toJson(_)})),
			"selected" -> Json.toJson(t.selected)
		))
	}
	implicit val writesPlayerEntity = new Writes[EntityPlayer] {
		def writes(entity:EntityPlayer):JsValue = JsObject(Seq(
			"kind"      -> JsString(entity.kind),
			"name"      -> JsString(entity.player.name),
			"hitPoints" -> JsNumber(entity.hitPoints)
		))
	}
	implicit val writesMobEntity:Writes[EntityMob] = new Writes[EntityMob] {
		def writes(t:EntityMob):JsValue = {
			JsObject(Seq(
				"kind" -> JsString(t.kind),
				"hitPoints" -> JsNumber(t.hitPoints)
			))
		}
	}
	implicit val writesBlockEntity:Writes[EntityBlock] = Json.writes[EntityBlock]
	implicit val writesPlayer = Json.writes[Player]
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	implicit val writesWorldEvent = Json.writes[WorldEvent]
}
