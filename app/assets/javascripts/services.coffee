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
			pub {id: "init"}
		north: -> pub {id: "north"}
		south: -> pub {id: "south"}
		east:  -> pub {id: "east"}
		west:  -> pub {id: "west"}
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
	appScope = null
	socket.setMessageCallback (message) ->
		switch message.id
			when "spawn" then do ->
				console.log("got a spawn message")
				appScope.spawnTime = new Date().getTime()
				if (!appScope.chunks?) then appScope.chunks = []
				appScope.chunks.push message.chunk
				appScope.$apply()
			else console.log("unknown message id: " + message.id)

	return (scope) -> appScope = scope
]

#
# Handles WebSocket communication with the server.
#
services.factory "socket", ["$window", ($window) ->
	wsUrl = $window.location.origin.replace(/^https/, "wss").replace(/^http/, "ws") + "/ws"
	ws = new $window.WebSocket wsUrl
	messageCallback = null # a function that handles all messages
	sendQueue = [] # messages to send after onopen

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
		message = $window.JSON.parse event.data
		messageCallback message if messageCallback?

	service =
		setMessageCallback: (fn) ->
			messageCallback = fn
		send: (message) ->
			json = $window.JSON.stringify(message)
			if !ws.readyState
				sendQueue.push json
			else
				ws.send json
]
