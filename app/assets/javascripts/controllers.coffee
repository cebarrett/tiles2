controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "net", ($scope, net) ->

	# define some important constants
	# (should this go here?)
	# FIXME: send the server side ones in init
	# FIXME: use jquery to get the css width of any tile
	$scope.tileSizePx = 30;	# also defined in LESS
	$scope.chunkLen = 16;	# also defined in server side and in LESS
	$scope.worldLen = 32;	# also defined in server side
	$scope.showLeftPanel = false

	# connect to the server
	net.connect $scope

	# view callbacks
	$scope.north = -> net.north()
	$scope.east  = -> net.east()
	$scope.south = -> net.south()
	$scope.west  = -> net.west()
	
	# gui select callback
	$scope.guiSelect = (index) ->
		if (index == 0)
			# assumes first choice is always the close button
			# and other choices should keep the GUI open
			delete $scope.guiOptions
		net.guiSelect(index)
	
	# tile click callback
	$scope.place = (x, y) ->
		net.place(x, y, $scope.selectedItemIndex)
		
	# select active item callback
	$scope.selectItem = (index) ->
		net.selectItem(index)
];
