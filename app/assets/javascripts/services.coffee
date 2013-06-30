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
			pub {kind: "spawn"}
		north: -> pub {kind: "north"}
		south: -> pub {kind: "south"}
		east:  -> pub {kind: "east"}
		west:  -> pub {kind: "west"}
		craft: (craft, index) ->
			pub {kind: "craft", craft: craft, index: index}
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

		if (message.player? and appScope.player? and message.player.name == appScope.player.name) then do ->
			appScope.player = message.player
			appScope.$apply()

		if message.tile? then do ->
			if (message.tile.entity? and appScope.player? and (message.tile.entity.name == appScope.player.name))
				appScope.playerEntity = message.tile.entity
				appScope.$apply()
			appScope.$broadcast('tileChange', message.x, message.y, message.tile)

		switch message.kind
			when "error" then do ->
				console.error("Error " + message.code + ": " + message.description)
			when "spawn" then do ->
				appScope.player = message.player
				appScope.crafts = message.crafts
				appScope.$apply()
			when "entityMove" then do ->
				if (message.prevX? && message.prevY?)
					oldchunk = _(appScope.chunks).find({
						cx: Math.floor(message.prevX/appScope.chunkLen)
						cy: Math.floor(message.prevY/appScope.chunkLen)
					})
					if oldchunk?
						prevTx = message.prevX-oldchunk.cx*appScope.chunkLen
						prevTy = message.prevY-oldchunk.cy*appScope.chunkLen
						prevTile = appScope.tileAt(message.prevX, message.prevY)
						delete prevTile.entity
						# broadcast the event so the chunk directive can re-render the tile
						appScope.$apply()
						appScope.$broadcast('tileChange', message.prevX, message.prevY, prevTile)
			when "chunk" then do ->
				appScope.loadChunk(message.chunk)
			when "chunkUnload" then do ->
				appScope.unloadChunk(message.cx, message.cy)
			when "playerDespawn" then do ->
				# FIXME: hack to log out dead players.
				# needs to be fixed on the server side too,
				# despawned players sending commands will cause server errors.
				if (message.player.name == appScope.player.name)
					window.location.replace(window.location.href)
			else null # other message types are just model syncing

	return (scope) -> appScope = scope
]

#
# Handles WebSocket communication with the server.
#
services.factory "socket", ["$window", ($window) ->
	wsUrl = $window.location.origin.replace(/^http/, "ws") + "/ws"
	ws = new $window.WebSocket wsUrl
	messageCallback = null # a function that handles all messages, null msg means disconnected
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

# manages a pool of chunk dom elements
services.factory "chunkManager", [ "tileRender", (tileRender) ->
	scope = null
	pool  = []
	newDomChunk = () ->
		# XXX: assumes 30px tile size
		$chunk = $('<div class="chunk"><div class="tile-column" style="left: 0px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 30px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 60px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 90px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 120px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 150px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 180px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 210px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 240px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 270px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 300px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 330px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 360px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 390px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div><div class="tile-column" style="left: 420px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div><div class="tile-column" style="left: 450px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div></div>')
		$chunk.find('.tile-column').each (tx) ->
			$tileCol = $(this)
			$tileCol.find('.tile').each (invTy) ->
				$tile = $(this)
				# FIXME: don't bind event listeners to each tile
				$tile.on 'selectstart', () -> false
				$tile.on 'click', (e) -> place $(this)
				$tile.on 'mouseover', (e) ->
					if e.which==1 then place $(this)
		$chunk
	while (pool.length < 16)
		pool.push newDomChunk()
	addChunkToDom = (chunk, $chunk) ->
		addCoordClass($chunk, chunk.cx, chunk.cy)
		$chunk.css "top", -((chunk.cy)*scope.tileSizePx*scope.chunkLen)+"px"
		$chunk.css "left", ((chunk.cx)*scope.tileSizePx*scope.chunkLen)+"px"
		$chunk.find('.tile-column').each (tx) ->
			$tileCol = $(this)
			$tileCol.find('.tile').each (invTy) ->
				$tile = $(this)
				ty = scope.chunkLen - invTy - 1
				tile = chunk.tiles[tx][ty]
				x = chunk.cx * scope.chunkLen + tx
				y = chunk.cy * scope.chunkLen + ty
				addCoordClass($tile, x, y)
				updateTile(tile, $tile)
		$('.world').append($chunk)
		$chunk
	updateTile = (tile, $tile) ->
		id = if tile.entity? then tile.entity.kind else tile.terrain.id
		render = tileRender[id];
		$tile.html "&#"+render.text.charCodeAt(0)+";"
		renderColor = do ->
			if (tile.entity? and tile.entity.material? and tile.entity.material.color?)
				tile.entity.material.color
			else
				render.color
		$tile.css {color: renderColor}
		pos = getCoordsFromCoordClass($tile)
		chunk = scope.chunkAt(pos.x, pos.y)
		chunk.tiles[tile.tx][tile.ty] = tile
		scope.$apply()
	place = ($tile) ->
		pos = getCoordsFromCoordClass($tile)
		if (pos? and scope.place?) then scope.place(pos.x, pos.y)
	testCoordClass = (str) -> /^-?\d+_-?\d+$/.test str
	removeCoordClass = ($el) -> 
		_classes = _($el.attr('class').split(' ')).filter(testCoordClass)
		_classes.each((c) -> $el.removeClass(c))
	addCoordClass = ($el, x, y) ->
		$el.addClass(x+"_"+y)
	getCoordsFromCoordClass = ($el) ->
		classAttr = $el.attr('class')
		classes = if classAttr? then classAttr.split(' ') else []
		coordClasses = (_(classes).filter (str) -> /^-?\d+_-?\d+$/.test(str)).value()
		if coordClasses.length > 0
			clazz = coordClasses[0]
			x = parseInt(clazz.split("_")[0], 10)
			y = parseInt(clazz.split("_")[1], 10)
			{x: x, y: y}
		else
			null
	service =
		loadChunk: (chunk) ->
			$chunk = pool.pop()
			addChunkToDom(chunk, $chunk)
		unloadChunk: (cx, cy) ->
			$domChunk = $('.'+cx+'_'+cy).filter('.chunk').detach()
			if ($domChunk.size() > 0)
				removeCoordClass($domChunk)
				$domChunk.find('.tile').each () -> removeCoordClass($(this))
				pool.push($domChunk)
		init: (s) ->
			scope = s
			# these events are broadcast by the sub service when a tile changes
			scope.$on 'tileChange', (something, x, y, tile) ->
				$tile = $('.'+x+'_'+y).filter('.tile')
				if $tile.size() > 0 then updateTile(tile, $tile)
]

services.factory "tileRender", [ () ->
	tileRender =
		player:
			text: "@"
			color: "white"
		llama:
			text: "L"
			color: "#BBBB99"
		goblin:
			text: "g"
			color: "#874"
		tree:
			text: "♣"
			color: "#00BB00"
		sapling:
			text: "τ"
			color: "#00BB00"
		sand:
			text: "·"
			color: "#DDBB77"
		dirt:
			text: "·"
			color: "#804828"
		bedrock:
			text: "·"
			color: "#444444"
		grass:
			text: "·"
			color: "green"
		floor:
			text: "+"
			color: "#605040"
		workbench:
			text: "π"
			color: "#555555"
		anvil:
			text: "Π"
			color: "#555555"
		kiln:
			text: "Ω"
			color: "#555555"
		smelter:
			text: "ʭ"
			color: "#555555"
		sawmill:
			text: "Ø"
			color: "#555555"
		stonecutter:
			text: "Θ"
			color: "#555555"
		block:
			text: "◼"
			color: "white"
		water:
			text: "≈"
			color: "#13E"
		lava:
			text: "≈"
			color: "#E31"
]
