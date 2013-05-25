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

#
# Handles WebSocket communication with the server.
#
services.factory "socket", ["$window", ($window) ->
	wsUrl = $window.location.origin.replace(/^https/, "wss").replace(/^http/, "ws") + "/ws"
	ws = new $window.WebSocket wsUrl
	messageCallback = null # a function that handles all messages
	sendQueue = [] # stuff messages here if not connected yet

	ws.onerror = (err) ->
		# TODO
		$window.console.error err
	ws.onopen = ->
		for json in sendQueue
			do (json) ->
				ws.send json
		sendQueue = []
	ws.onclose = (err) ->
		# TODO
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
				sendQueue.push json
			else
				ws.send json
]

##
## Deprecated Services
##

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
			randTerrainId = -> if (Math.random() > 0.5) then "dirt" else "water"
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
							id: randTerrainId()
						entity: null
					}
				{
					cx: cx
					cy: cy
					tiles: randTileForIndex i for i in [1..chunkLen*chunkLen]
				}
			randChunkForIndex i for i in [1..worldLen*worldLen];
		# mock player and inventory
		scope.player =
			name: "mock_player"
			x: 25
			y: 25
			inventory: [
				{count: 1, item: "Wooden Axe"}
				{count: 9, item: "Apple"}
			]
		# place some mock entities
		scope.world.tileAt(scope.player.x, scope.player.y).entity =
			id: "player"
			player: scope.player
		scope.world.tileAt(10,10).entity =
			id: "tree"
		scope.world.tileAt(10,20).entity =
			id: "hydra"
			
	return {populate: populate}
]

