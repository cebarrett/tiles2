controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "net", ($scope, net) ->

	# define some important constants
	# (should this go here?)
	$scope.tileSizePx = 40;	# also defined in LESS
	$scope.chunkLen = 16;	# also defined in server side and in LESS
	$scope.worldLen = 2;	# also defined in server side

	# connect to the server
	net.connect $scope

	# view callbacks
	$scope.north = -> net.north()
	$scope.east  = -> net.east()
	$scope.south = -> net.south()
	$scope.west  = -> net.west()
	
	# FIXME: populate some stuff for testing
	$scope.gui = [
		"Close",
		"Craft 4 wood from 1 log",
		"Craft a wooden axe from 1 stick and 1 wood"
	];
	$scope.guiSelect = (index) ->
		console.log("Selected: " + $scope.gui[index])
		(delete $scope.gui) if (index == 0)
];
