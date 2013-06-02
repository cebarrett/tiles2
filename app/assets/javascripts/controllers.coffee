controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "$log", "net", ($scope, $log, net) ->

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
	$scope.guiOptions = [
		"Close",
		"Craft 4 wood from 1 log",
		"Craft a wooden axe from 1 stick and 1 wood"
	];
	$scope.guiSelect = (index) ->
		$log.info("GUI selection: " + $scope.guiOptions[index])
		if (index == 0)
			# assumes first choice is always the close button
			# and other choices should keep the GUI open
			delete $scope.guiOptions
		net.guiSelect(index)
];
