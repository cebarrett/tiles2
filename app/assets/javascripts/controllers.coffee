controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "server", ($scope, server) ->

	# view callbacks
	$scope.north = -> server.north()
	$scope.east = -> server.east()
	$scope.south = -> server.south()
	$scope.west = -> server.west()
	
	# init the world
	server.init $scope

];
