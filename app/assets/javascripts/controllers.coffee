controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$rootScope", "$scope", "$log", "server", ($rootScope, $scope, $log, server) ->
	$scope.tileSizePx = 12;	# also defined in LESS
	$rootScope.chunkLen = 16;	# also defined in LESS
	$rootScope.worldLen = 3;
	$rootScope.player = {
		name: "mock_player"
		x: 0
		y: 0
		inventory: 	[
			{count: 1, item: "Sword"},
			{count: 9, item: "Apple"}
		]
	}
	$rootScope.chunks = do ->
		# generate some mock chunk data
		chunkLen = $rootScope.chunkLen;
		worldLen = $rootScope.worldLen;
		randTerrain = -> {id: Math.round Math.random()}
		randChunkForIndex = (i) ->
			cx = Math.floor((i-1)/worldLen)
			cy = (i-1)%worldLen
			randTileForIndex = (i) ->
				tx = Math.floor((i-1)/chunkLen)
				ty = (i-1)%chunkLen
				{
					tx: tx
					ty: ty
					terrain: randTerrain()
					entity: do ->
						if tx is 0 and ty is 0 and cx is 0 and cy is 0 then {id: 0} else null
				}
			{
				cx: cx
				cy: cy
				tiles: randTileForIndex i for i in [1..chunkLen*chunkLen]
			}
		randChunkForIndex i for i in [1..worldLen*worldLen];
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
