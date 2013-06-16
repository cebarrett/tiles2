filters = angular.module "app.filters", []

filters.filter "recipeFilter", [ () ->
	(recipe) ->
		"Craft some " + recipe.result.kind + " made of something from some stuff"
]
