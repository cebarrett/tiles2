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
			scope.chunks = []
			sub scope
		north: -> pub {kind: "north"}
		south: -> pub {kind: "south"}
		east:  -> pub {kind: "east"}
		west:  -> pub {kind: "west"}
		guiSelect: (index) ->
			pub {kind: "guiSelect", index: index}
		place: (x, y, index) ->
			pub {kind: "place", x: x, y: y, index: index}
		selectItem: (index) ->
			pub {kind: "selectItem", index: index}
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
services.factory "sub", ["socket", (socket) ->
	# FIXME: spaghetti code
	appScope = null
	socket.setMessageCallback (message) ->
		if (message == null)
			appScope.connected = false
			appScope.$apply()
			return
		appScope.connected = true
		chunk = null
		tile = null
		newChunk = null
		if message.tile? then do ->
			chunk = _(appScope.chunks).find({
				cx: Math.floor(message.x/appScope.chunkLen)
				cy: Math.floor(message.y/appScope.chunkLen)
			})
			if (chunk?)
				chunk.tiles[message.tile.tx][message.tile.ty] = message.tile
				tile = chunk.tiles[message.tile.tx][message.tile.ty]
				appScope.$apply()
				# broadcast the event so the chunk directive can re-render the tile
				appScope.$broadcast('tileChange', message.x, message.y, tile)
		if (message.player? and appScope.player? and message.player.name == appScope.player.name) then do ->
			appScope.player = message.player
			appScope.$apply()
		switch message.kind
			when "error" then do ->
				console.error("Error " + message.code + ": " + message.description)
			when "spawn" then do ->
				appScope.player = message.player
				appScope.$apply()
			when "gui" then if message.player.name == appScope.player.name
				appScope.guiOptions = message.options
				appScope.$apply()
			# when "playerSpawn" then console.log("player spawned")
			# when "playerDespawn" then console.log("player despawned")
			# when "entityDespawn" then console.log("entity despawned")
			# when "playerUpdate" then console.log("player update")######
			when "entityMove" then do ->
				if (message.prevX? && message.prevY?)
					oldchunk = _(appScope.chunks).find({
						cx: Math.floor(message.prevX/appScope.chunkLen)
						cy: Math.floor(message.prevY/appScope.chunkLen)
					})
					if oldchunk?
						prevTx = message.prevX-oldchunk.cx*appScope.chunkLen
						prevTy = message.prevY-oldchunk.cy*appScope.chunkLen
						prevTile = oldchunk.tiles[prevTx][prevTy]
						delete prevTile.entity
						# broadcast the event so the chunk directive can re-render the tile
						appScope.$broadcast('tileChange', message.prevX, message.prevY, prevTile)
			when "chunk" then do ->
				appScope.chunks.push(message.chunk)
			when "chunkUnload" then do ->
				appScope.chunks = appScope.chunks.filter (chunk) ->
					!((chunk.cx == message.cx) && (chunk.cy == message.cy))
			# else console.log("unknown kind of message: " + message.kind)

	return (scope) -> appScope = scope
]

#
# Handles WebSocket communication with the server.
#
services.factory "socket", ["$window", ($window) ->
	wsUrl = $window.location.origin.replace(/^http/, "ws") + "/ws"
	ws = new $window.WebSocket wsUrl
	messageCallback = null # a function that handles all messages, null means disconnected
	sendQueue = [] # messages to send after onopen
	receiveQueue = [] # messages to receive after messageCallback is set

	ws.onerror = (err) ->
		$window.console.error err
	ws.onopen = ->
		ws.send(json) for json in sendQueue
		sendQueue = []
	ws.onclose = (err) ->
		$window.console.error err
		messageCallback(null) if messageCallback?
	ws.onmessage = (event) ->
		message = $window.JSON.parse event.data
		if messageCallback?
			messageCallback message 
		else
			receiveQueue.push message

	service =
		setMessageCallback: (fn) ->
			messageCallback = fn
			messageCallback(json) for json in receiveQueue
			receiveQueue = []
		send: (message) ->
			json = $window.JSON.stringify(message)
			if !ws.readyState
				sendQueue.push json
			else
				ws.send json
]

services.factory "tileRender", [ () ->
	tileRender =
		player:
			text: "@"
			color: "white"
		tree:
			text: "♠"
			color: "#00BB00"
		sand:
			text: "."
			color: "#CCEEAA"
		dirt:
			text: "."
			color: "#885030"
		rock:
			text: "."
			color: "#777777"
		workbench:
			text: "π"
			color: "#BB7722"
		wood:
			text: "#"
			color: "#BB7722"
		sapling:
			text: "τ"
			color: "#00BB00"
		llama:
			text: "L"
			color: "#BBBB99"
		stone:
			text: "#"
			color: "#777777"
		ore:
			text: "$"
			color: "#777777"
		furnace:
			text: "ʭ"
			color: "#777777"
		stonecutter:
			text: "Ø"
			color: "#585858"
		sawmill:
			text: "Ø"
			color: "#AA6600"
]
