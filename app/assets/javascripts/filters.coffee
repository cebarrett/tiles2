filters = angular.module "app.filters", []

filters.filter "recipeFilter", [ () ->
	itemFilter = (stack) ->
		(if stack.count? then stack.count+" " else "a ") +
		(if stack.item.material? then stack.item.material.kind+" " else "") +
		stack.item.kind


	(recipe) ->
		string = "Craft " + itemFilter(recipe.result) + " from";
		_(recipe.ingredients).each((item, i) -> string = string + " " + itemFilter(item)+(if (i==recipe.ingredients.length-1) then "" else ","));
		string
]
