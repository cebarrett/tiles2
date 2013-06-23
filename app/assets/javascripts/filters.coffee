filters = angular.module "app.filters", []

filters.filter "recipeFilter", [ () ->
	itemFilter = (ingredient) ->
		string = (if ingredient.count? then ingredient.count+" " else "a ")
		if (ingredient.material?)
			string += ingredient.material.kind
		else if (ingredient.item?)
			string += (if ingredient.item.material? then ingredient.item.material.kind+" " else "")
			string += ingredient.item.kind


	(recipe) ->
		string = "Craft " + itemFilter(recipe.result) + " from";
		_(recipe.ingredients).each((item, i) -> string = string + " " + itemFilter(item)+(if (i==recipe.ingredients.length-1) then "" else ","));
		string
]
