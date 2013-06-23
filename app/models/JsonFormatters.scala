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
//	implicit val writesTreeEntity:Writes[EntityTree] = Json.writes[EntityTree]
//	implicit val writesWorkbenchEntity:Writes[EntityWorkbench] = Json.writes[EntityWorkbench]
//	implicit val writesSaplingEntity:Writes[EntitySapling] = Json.writes[EntitySapling]
	implicit val writesMobEntity:Writes[EntityMob] = new Writes[EntityMob] {
		def writes(t:EntityMob):JsValue = {
			JsObject(Seq(
				"kind" -> JsString(t.kind),
				"hitPoints" -> JsNumber(t.hitPoints)
			))
		}
	}
//	implicit val writesKilnEntity:Writes[EntityKiln] = Json.writes[EntityKiln]
//	implicit val writesSmelterEntity:Writes[EntitySmelter] = Json.writes[EntitySmelter]
//	implicit val writesSawmillEntity:Writes[EntitySawmill] = Json.writes[EntitySawmill]
//	implicit val writesStonecutterEntity:Writes[EntityStonecutter] = Json.writes[EntityStonecutter]
//	implicit val writesAnvilEntity:Writes[EntityAnvil] = Json.writes[EntityAnvil]
	implicit val writesBlockEntity:Writes[EntityBlock] = Json.writes[EntityBlock]
//	implicit val writesEntity = new Writes[Entity] {
//		def writes(t:Entity):JsValue = t match {
//			case _:EntityPlayer => writesPlayerEntity.writes(t.asInstanceOf[EntityPlayer])
//			case _:EntityTree   => writesTreeEntity.writes(t.asInstanceOf[EntityTree])
//			case _:EntityWorkbench =>  writesWorkbenchEntity.writes(t.asInstanceOf[EntityWorkbench])
//			case _:EntitySapling => writesSaplingEntity.writes(t.asInstanceOf[EntitySapling])
//			case _:EntitySawmill => writesSawmillEntity.writes(t.asInstanceOf[EntitySawmill])
//			case _:EntityStonecutter => writesStonecutterEntity.writes(t.asInstanceOf[EntityStonecutter])
//			case entity:EntityMob => Json.toJson(entity)
//			case entity:EntityKiln => Json.toJson(entity)
//			case entity:EntitySmelter => Json.toJson(entity)
//			case entity:EntityAnvil => Json.toJson(entity)
//			case entity:EntityBlock => Json.toJson(entity)
//			case item:Item => Json.toJson(item)
//			case _ => {
//				val msg = "writesEntity: Unknown entity class: " + t.getClass
//				Logger.error(msg)
//				JsUndefined(msg)
//			}
//		}
//	}
//	implicit val writesOptionEntity = new Writes[Option[Entity]] {
//		def writes(t:Option[Entity]):JsValue = {
//			if (t.isDefined) {
//				writesEntity.writes(t.head)
//			} else {
//				JsNull
//			}
//		}
//	}
	implicit val writesPlayer = Json.writes[Player]
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	implicit val writesWorldEvent = Json.writes[WorldEvent]
}
