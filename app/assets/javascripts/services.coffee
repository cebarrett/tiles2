services = angular.module "app.services", []

# FIXME: refactor
# split this service into two services, publisher and subscriber.
# subscriber can hold a $scope but publisher should not.
# both can depend on a third service that handles networking.
# also move chunk/tile utility methods elsewhere
services.factory "server", ["$log", ($log) ->
	scope = null

	init = (theScope) ->
		scope = theScope
		scope.player = {
			# FIXME: this should all be part of the player entity
			name: "mock_player"
			x: 25
			y: 25
			inventory: 	[
				{count: 1, item: "Naginata"},
				{count: 9, item: "Kiwi"}
			]
		}
		scope.chunks = do ->
			# generate some mock chunk data
			chunkLen = scope.chunkLen;
			worldLen = scope.worldLen;
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
							# player entity
							if tx is 9 and ty is 9 and cx is 1 and cy is 1 then {id: 0} else null
					}
				{
					cx: cx
					cy: cy
					tiles: randTileForIndex i for i in [1..chunkLen*chunkLen]
				}
			randChunkForIndex i for i in [1..worldLen*worldLen];
	use = ->
		$log.log "server: use invoked"
	move = (x, y) ->
		$log.log "server: move invoked [x="+x+" y="+y+"]";
		
		player = scope.player;
		worldLen = scope.worldLen;
		chunkLen = scope.chunkLen;
		
		tileAt = (x, y) ->
			cx = Math.floor(x / chunkLen);
			cy = Math.floor(y / chunkLen);
			tx = (x + chunkLen) % chunkLen;
			ty = (y + chunkLen) % chunkLen;
			chunk = scope.chunks[cx*worldLen+cy]
			if chunk?
				chunk.tiles[tx*chunkLen+ty];
			else
				null
		prevTile = tileAt(player.x, player.y)
		nextTile = tileAt(player.x+x, player.y+y);
		if prevTile? and nextTile? and !(nextTile.entity?)
			nextTile.entity = prevTile.entity;
			prevTile.entity = null;
			scope.player.x += x;
			scope.player.y += y;
			scope.$apply()
	{
		north: () -> move 0,1
		south: () -> move 0,-1
		east: () -> move 1,0
		west: () -> move -1,0
		use: use
		init: init
	}
];

services.factory "mockdata", [->
	{
		addToScope: (scope) ->
	}
]
