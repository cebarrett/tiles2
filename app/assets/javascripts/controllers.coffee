controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "net", ($scope, net) ->

	# define some important constants
	# (should this go here?)
	$scope.tileSizePx = 32;	# also defined in LESS
	$scope.chunkLen = 16;	# also defined in LESS
	$scope.worldLen = 3;

	# connect to the server
	net.connect $scope

	# view callbacks
	$scope.north = -> net.north()
	$scope.east  = -> net.east()
	$scope.south = -> net.south()
	$scope.west  = -> net.west()
];
