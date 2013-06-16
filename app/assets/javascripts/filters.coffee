filters = angular.module "app.filters", []

filters.filter "recipeFilter", [ () ->
	(recipe) ->
		string = "Craft " +
			(if recipe.result.count? then recipe.result.count+" " else "a ") +
			(if recipe.result.material? then recipe.result.material.kind+" " else "") +
			recipe.result.kind+" " + 
			"from";
		_(recipe.ingredients).each((item, i) -> string = string + " something,");
		string
]
