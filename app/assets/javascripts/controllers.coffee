controllers = angular.module "app.controllers", []

controllers.controller "AppCtrl", ["$scope", ($scope) ->
	$scope.tileSizeEm = 0.5;	# also defined in LESS
	$scope.chunkLen = 16;		# also defined in LESS
	$scope.player = {
		name: "mock_player"
		inventory: 	[
			{count: 1, item: "Sword"},
			{count: 9, item: "Apple"}
		]
	}
	$scope.chunks = do ->
		# generate some mock chunk data
		worldLen = 4;
		chunkLen = $scope.chunkLen;
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
];
