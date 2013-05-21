controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "server", "mockdata", ($scope, server, mockdata) ->

	$scope.tileSizePx = 12;	# also defined in LESS
	$scope.chunkLen = 16;	# also defined in LESS
	$scope.worldLen = 3;
	
	mockdata.addToScope($scope)
	
	server.init($scope)
	
	$scope.north = ->
		server.north()
	$scope.east = ->
		server.east();
	$scope.south = ->
		server.south();
	$scope.west = ->
		server.west();
	$scope.use = ->
		server.use();
];
