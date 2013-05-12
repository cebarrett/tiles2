app = angular.module "app", []

app.config ["$routeProvider", ($routeProvider) ->
	$routeProvider.when '', {templateUrl: "assets/partials/app.html", controller: "AppCtrl"}
]
app.controller "AppCtrl", ["$scope", ($scope) ->
	$scope.name = ""
];
