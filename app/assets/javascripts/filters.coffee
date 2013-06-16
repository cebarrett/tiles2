filters = angular.module "app.filters", []

filters.filter "recipeFilter", [ () ->
	itemFilter = (item) ->
		(if item.count? then item.count+" " else "a ") +
		(if item.material? then item.material.kind+" " else "") +
		item.kind


	(recipe) ->
		string = "Craft " + itemFilter(recipe.result) + " from";
		_(recipe.ingredients).each((item, i) -> string = string + " " + itemFilter(item)+(if (i==recipe.ingredients.length-1) then "" else ","));
		string
]
