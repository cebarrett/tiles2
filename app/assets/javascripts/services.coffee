services = angular.module "app.services", []

#
# Service that handles communication with the server.
# All communication with the game server is asynchronous publish/subscribe - no RPC.
# The connect(scope) method must be invoked before calling any other
# method, and accepts one parameter, a scope that will be
# populated with all model data received from the server.
# To send messages to the server, call the other methods on this object.
#
services.factory "net", ["pub", "sub", (pub, sub) ->
	service =
		connect: (scope) ->
			sub scope
			pub {message: "init"}
		north: -> pub {message: "north"}
		south: -> pub {message: "south"}
		east:  -> pub {message: "east"}
		west:  -> pub {message: "west"}
]

#
# Sends messages to the server.
#
services.factory "pub", ["socket", (socket) ->
	(object) -> socket.send object
]

#
# Listens for game events from the server and 
# updates the model data in the application
# controller's scope.
#
services.factory "sub", ["socket", "mock", (socket, mock) ->
	appScope = null
	socket.setMessageCallback (message) ->
		console.log(message)	# TODO

	return (scope) ->
			appScope = scope
			mock.populate(appScope)
]

services.factory "socket", ["$window", ($window) ->
	wsUrl = $window.location.origin.replace(/^https/, "wss").replace(/^http/, "ws") + "/ws"
	ws = new $window.WebSocket wsUrl

	messageCallback = null

	ws.onerror = (err) ->
		# FIXME
		$window.console.error err
	ws.onclose = (err) ->
		# FIXME
		$window.console.error err
	ws.onmessage = (event) ->
		msg = $window.JSON.parse event.data
		messageCallback msg if messageCallback?

	service =
		setMessageCallback: (obj) ->
			messageCallback = obj
		send: (object) ->
			json = $window.JSON.stringify(object)
			if !ws.readyState
				# FIXME !!!
				$window.console.warn "discarding message, socket not ready: " + json
			else
				ws.send json
]

##
## Deprecated Services
##

# FIXME: refactor
# split this service into two services, publisher and subscriber.
# subscriber can hold a $scope but publisher should not.
# both can depend on a third service that handles networking.
# also move chunk/tile utility methods elsewhere
services.factory "server", ["$log", "mock", ($log, mock) ->
	scope = null

	init = (theScope) ->
		scope = theScope
		# populate mock data
		mock.populate scope

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
		init: init
	}
];

services.factory "mock", [->
	populate = (scope) ->

		# mock world object, should own the chunks but
		# for now this just holds some clunky utility methods
		scope.world = 
			chunkAt: (x, y) ->
				cx = Math.floor (x / scope.chunkLen);
				cy = Math.floor (y / scope.chunkLen);
				chunk = null
				for obj in scope.chunks
					do (obj) ->
						if obj.cx == cx and obj.cy == cy then chunk = obj;
				return chunk
			tileAt: (x, y) ->
				chunk = this.chunkAt x, y
				tx = ((scope.chunkLen)+(x%scope.chunkLen))%scope.chunkLen
				ty = ((scope.chunkLen)+(y%scope.chunkLen))%scope.chunkLen
				console.log [x,y]
				tile = null
				for obj in chunk.tiles
					do (obj) ->
						if obj.tx == tx and obj.ty == ty then tile = obj;
				return tile
		# generate some mock chunk data
		scope.chunks = do ->
			chunkLen = scope.chunkLen;
			worldLen = scope.worldLen;
			randTerrainType = -> if (Math.random() > 0.5) then "dirt" else "water"
			randChunkForIndex = (i) ->
				cx = Math.floor((i-1)/worldLen)
				cy = (i-1)%worldLen
				randTileForIndex = (i) ->
					tx = Math.floor((i-1)/chunkLen)
					ty = (i-1)%chunkLen
					{
						tx: tx
						ty: ty
						terrain:
							type: randTerrainType()
						entity: null
					}
				{
					cx: cx
					cy: cy
					tiles: randTileForIndex i for i in [1..chunkLen*chunkLen]
				}
			randChunkForIndex i for i in [1..worldLen*worldLen];
		# mock player and inventory
		scope.player = {
			name: "mock_player"
			x: 25
			y: 25
			inventory: [
				{count: 1, item: "Naginata"}
				{count: 9, item: "Kiwi"}
			]
		}
		# place some mock entities
		scope.world.tileAt(scope.player.x, scope.player.y).entity = {
			type: "player"
			player: scope.player
		}
		scope.world.tileAt(10,10).entity = {
			type: "tree"
		}
		scope.world.tileAt(10,20).entity =
			type: "hydra"
			
	return {populate: populate}
]

