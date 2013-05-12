app = angular.module "app", []

app.config ["$routeProvider", ($routeProvider) ->
	$routeProvider.when '', {templateUrl: "assets/partials/app.html", controller: "AppCtrl"}
]

app.controller "AppCtrl", ["$scope", ($scope) ->
	$scope.tileSizeEm = 0.5;	# also defined in LESS
	$scope.chunkLen = 16;		# also defined in LESS
	$scope.inventory = [
		{count: 1, item: "Sword"},
		{count: 9, item: "Apple"},
		{count: 239, item: "Stone"},
	]
	$scope.chunks = do ->
		# generate some mock chunk data
		worldLen = 4;
		chunkLen = $scope.chunkLen;
		randChunkForIndex = (i) ->
			cx = Math.floor((i-1)/worldLen)
			cy = (i-1)%worldLen
			randTileForIndex = (i) ->
				tx = Math.floor((i-1)/chunkLen)
				ty = (i-1)%chunkLen
				{
					tx: tx
					ty: ty
					id: Math.round Math.random()
				}
			{
				cx: cx
				cy: cy
				tiles: randTileForIndex i for i in [1..chunkLen*chunkLen]
			}
		randChunkForIndex i for i in [1..worldLen*worldLen];
];
