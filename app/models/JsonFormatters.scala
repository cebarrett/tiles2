package models;

import play.api.libs.json._
import play.api.Logger

/**
 * Implicits to help with JSON formatting.
 */
object JsonFormatters {
	
	// material
	implicit val writesMaterial = new Writes[Material] {
		def writes(arg:Material):JsValue = {
			JsObject(Seq(
				"kind" -> JsString(arg.kind),
				"color" -> JsString(arg.color)
			))
		}
	}
	
	// items
	implicit val writesMaterialItem = new Writes[ItemWithMaterial] {
		def writes(item:ItemWithMaterial):JsValue = JsObject(
			Seq("kind" -> Json.toJson(item.kind)) ++ {
				if (item.material == null)
					Seq.empty
				else
					Seq("material" -> Json.toJson(item.material))
			}
		)
	}
	implicit val writesPlayerEntity = new Writes[EntityPlayer] {
		def writes(entity:EntityPlayer):JsValue = JsObject(Seq(
			"kind"      -> JsString(entity.kind),
			"name"      -> JsString(entity.player.name),
			"hitPoints" -> JsNumber(entity.hitPoints)
		) ++ (if (entity.player.armor.isEmpty) Seq.empty else Seq(
			"material"  -> Json.toJson(entity.player.armor.get.material)
		)))
	}
	implicit val writesLivingEntity:Writes[EntityLiving] = new Writes[EntityLiving] {
		def writes(t:EntityLiving):JsValue = {
			JsObject(Seq(
				"kind" -> JsString(t.kind),
				"hitPoints" -> JsNumber(t.hitPoints)
			))
		}
	}
	implicit val writesItem = new Writes[Item] {
		def writes(item:Item):JsValue = item match {
			case item:EntityPlayer      => writesPlayerEntity.writes(item)
			case item:EntityLiving      => writesLivingEntity.writes(item)
			case item:ItemWithMaterial  => writesMaterialItem.writes(item)
			case _                      => JsObject(Seq("kind" -> JsString(item.kind)))
		}
	}
	
	// inventory
	implicit val writesItemStack = Json.writes[ItemStack]
	implicit val writesInventory = new Writes[Inventory] {
		def writes(t:Inventory):JsValue = JsObject(Seq(
			"items" -> JsArray(t.items.map({Json.toJson(_)})),
			"selected" -> Json.toJson(t.selected)
		))
	}
	
	// crafting
	implicit val writesIngredient = new Writes[Ingredient] {
		def writes(obj:Ingredient):JsValue = {
			obj match {
				case obj:IngredientMaterial[_] => {
					JsObject(Seq(
						"kind"  -> JsString(obj.material.getSimpleName.toLowerCase.replaceAll("\\$*$", "")),
						"count" -> JsNumber(obj.count)))}
				case obj:IngredientItem => {
					JsObject(Seq(
						"kind"  -> JsString(obj.item.kind),
						"count" -> JsNumber(obj.count)))}
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
	
	// player
	implicit val writesPlayer = Json.writes[Player]
	
	// world
	implicit val writesTerrain = new Writes[Terrain] {
		def writes(terrain:Terrain):JsValue = JsObject(Seq(
			"id"     -> Json.toJson(terrain.id),
			"passable" -> Json.toJson(terrain.passable)
		))
	}
	implicit val writesTile = Json.writes[Tile]
	implicit val writesChunk = Json.writes[Chunk]
	
	// event
	implicit val writesWorldEvent = Json.writes[WorldEvent]
	
	// misc entities
	implicit val writesBlockEntity:Writes[EntityBlock] = Json.writes[EntityBlock]
}
