app = angular.module "app", ["app.directives", "app.controllers"]

app.config ["$routeProvider", ($routeProvider) ->
	$routeProvider.when '', {templateUrl: "assets/partials/app.html", controller: "AppCtrl"}
]
