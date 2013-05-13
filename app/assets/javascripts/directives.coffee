directives = angular.module "app.directives", []
directives.directive "appControls", ->
	(scope, elm, attrs) ->
		# TODO
		console.log("appControls directive ran")
		elm[0].addEventListener 'keydown', (e) ->
			console.log e
