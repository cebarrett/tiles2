controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "net", ($scope, net) ->
	net.init $scope
	$scope.north = -> net.north()
	$scope.east  = -> net.east()
	$scope.south = -> net.south()
	$scope.west  = -> net.west()
];
