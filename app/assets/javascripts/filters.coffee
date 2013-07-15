filters = angular.module "app.filters", []

filters.filter "itemFilter", [ () ->
	(itemKind) -> if itemKind == "block" then "" else itemKind
]

filters.filter "recipeFilter", [ () ->
	ingredientFilter = (ingredient) ->
		string = (if ingredient.count? then ingredient.count+" " else "a ")
		if (ingredient.kind?)
			string += ingredient.kind
		else if (ingredient.item?)
			string += (if ingredient.item.material? then ingredient.item.material.kind+" " else "")
			string += ingredient.item.kind
		string


	(recipe) ->
		string = "Craft " + ingredientFilter(recipe.result) + " from";
		_(recipe.ingredients).each((item, i) -> 
			string = string +
					" " +
					ingredientFilter(item) +
					(if (i==recipe.ingredients.length-1) then "" else ",")
		);
		string
]
