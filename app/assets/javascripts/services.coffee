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
			pub {kind: "spawn"}
		north: -> pub {kind: "north"}
		south: -> pub {kind: "south"}
		east:  -> pub {kind: "east"}
		west:  -> pub {kind: "west"}
		craft: (craft, index) ->
			pub {kind: "craft", craft: craft, index: index}
		place: (x, y) ->
			pub {kind: "place", x: x, y: y}
		selectItem: (index) ->
			pub {kind: "selectItem", index: index}
		swap: (i0, i1) ->
			pub {kind: "swap", i0: i0, i1: i1}
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
	scope = null
	
	socket.setMessageCallback (message) ->
		apply = false # whether to call scope.$apply when done
		scope.connected = true
		if (message == null)
			# null message means we were disconnected
			scope.connected = false
			scope.$apply()
			return
		if (message.time? and message.time != scope.timeStr)
			scope.timeStr = message.time
		if (message.player? and scope.player? and message.player.name == scope.player.name) then do ->
			scope.player = message.player
			# optimization
			if (message.kind != "entityMove") then apply = true
		if message.tile? then do ->
			if (message.tile.entity? and scope.player? and (message.tile.entity.name == scope.player.name))
				scope.player.x = message.x
				scope.player.y = message.y
				scope.playerEntity = message.tile.entity
				apply = true
			scope.$broadcast('tileChange', message.x, message.y, message.tile)
		switch message.kind 
			when "error"
				console.error("Error " + message.code + ": " + message.description)
			when "spawn"
				scope.player = message.player
				scope.crafts = message.crafts
				apply = true
			when "entityMove"
				if (message.prevX? && message.prevY?)
					oldchunk = scope.chunkAt(message.prevX, message.prevY)
					if oldchunk?
						prevTx = message.prevX-oldchunk.cx*scope.chunkLen
						prevTy = message.prevY-oldchunk.cy*scope.chunkLen
						prevTile = scope.tileAt(message.prevX, message.prevY)
						delete prevTile.entity
						# broadcast the event so the chunk directive can re-render the tile
						scope.$broadcast('tileChange', message.prevX, message.prevY, prevTile)
						apply = true
			when "chunk"
				scope.loadChunk(message.chunk)
			when "chunkUnload"
				scope.unloadChunk(message.cx, message.cy)
			when "playerDespawn"
				# FIXME: hack to log out dead players.
				# needs to be fixed on the server side too,
				# despawned players sending commands will cause server errors.
				if (message.player.name == scope.player.name)
					window.location.replace(window.location.href)
			else null # other message types are just model syncing
		if apply == true then scope.$apply()
	
	return (sc) -> scope = sc
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
	# all loaded chunks in the game
	chunks = []
	# pool of reusable chunk dom elements
	# FIXME: these are being added to the dom on chunk load
	pool  = []
	newDomChunk = () ->
		# XXX: assumes 30px tile size
		$chunk = $('<div class="chunk"><div class="tile-column" style="left: 0px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 30px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 60px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 90px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 120px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 150px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 180px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 210px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 240px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 270px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 300px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 330px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 360px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">·</div></div><div class="tile-column" style="left: 390px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div><div class="tile-column" style="left: 420px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div><div class="tile-column" style="left: 450px;"><div class="tile" style="top: -450px;"></div><div class="tile" style="top: -420px;"></div><div class="tile" style="top: -390px;"></div><div class="tile" style="top: -360px;"></div><div class="tile" style="top: -330px;"></div><div class="tile" style="top: -300px;"></div><div class="tile" style="top: -270px;"></div><div class="tile" style="top: -240px;"></div><div class="tile" style="top: -210px;"></div><div class="tile" style="top: -180px;"></div><div class="tile" style="top: -150px;"></div><div class="tile" style="top: -120px;"></div><div class="tile" style="top: -90px;"></div><div class="tile" style="top: -60px;"></div><div class="tile" style="top: -30px;"></div><div class="tile" style="top: -0px;">◼</div></div></div>')
		$chunk.find('.tile-column').each (tx) ->
			$tileCol = $(this)
			$tileCol.find('.tile').each (invTy) ->
				$tile = $(this)
				# FIXME: don't bind event listeners to each tile
				$tile.on 'click mouseover', (e) ->
					if e.which==1 then place $(this)
		$chunk
	while (pool.length < 32)
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
	place = ($tile) ->
		pos = getCoordsFromCoordClass($tile)
		if (pos? and scope.place?) then scope.place(pos.x, pos.y)
	testCoordClass = (str) -> /^-?\d+_-?\d+$/.test str
	removeCoordClass = ($el) -> 
		_classes = _($el.attr('class').split(' ')).filter(testCoordClass)
		_classes.each((c) -> $el.removeClass(c))
		$el.removeAttr('x')
		$el.removeAttr('y')
	addCoordClass = ($el, x, y) ->
		$el.addClass(x+"_"+y)
		$el.attr('x', x)
		$el.attr('y', y)
	getCoordsFromCoordClass = ($el) ->
		if ($el.attr('x')?)
			{x: parseInt($el.attr('x'),10), y: parseInt($el.attr('y'),10)}
		else
			null
	service =
		loadChunk: (chunk) ->
			chunks.push(chunk)
			$chunk = pool.pop()
			addChunkToDom(chunk, $chunk)
		unloadChunk: (cx, cy) ->
			chunks = _(chunks).reject({cx:cx, cy:cy}).value()
			$domChunk = $('.'+cx+'_'+cy).filter('.chunk')
			$domChunk.detach()
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
		chunkAt: (cx, cy) ->
			_(chunks).find({cx:cx, cy:cy})
]

services.factory "renderLoop", [ () ->
	callbacks = _([])
	step = () ->
		callbacks.each (cb) -> cb()
		requestAnimationFrame(step)
	step()
	service =
		addCallback: (fn) ->
			callbacks.push(fn)
		removeCallback: (fn) ->
			callbacks = callbacks.without(fn)
]

services.factory "chunkQueue", [ "renderLoop", "chunkManager", (renderLoop, chunkManager) ->
	queue = []
	renderLoop.addCallback () ->
		if queue.length > 0
			task = queue[0]
			queue = queue.splice 1
			if task.tiles? then chunkManager.loadChunk task
			else chunkManager.unloadChunk task.cx, task.cy
	service =
		loadChunk: (chunk) ->
			queue.push(chunk)
		unloadChunk: (cx, cy) ->
			queue.push(cx: cx, cy: cy)
]

services.factory "tileRender", [ () ->
	tileRender =
		player:
			text: "@"
			color: "white"
		pig:
			text: "p"
			color: "rgb(221, 164, 193)"
		spider:
			text: "s"
			color: "#737370"
		goblin:
			text: "g"
			color: "rgb(167, 132, 81)"
		dragon:
			text: "D"
			color: "#D11"
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
		snow:
			text: "·"
			color: "#eee"
		floor:
			text: "+"
			color: "#68583E"
		door:
			text: "÷"
			color: "#68583E"
		water:
			text: "≈"
			color: "#13E"
		lava:
			text: "≈"
			color: "#E31"
		food:
			text: "❤"
			color: "#C22"
		workbench:
			text: "π"
		workshop:
			text: "Π"
		forge:
			text: "Ƌ"
		kiln:
			text: "Ω"
		furnace:
			text: "ʬ"
		smelter:
			text: "ʭ"
		sawmill:
			text: "Ø"
		stonecutter:
			text: "Θ"
		gemcutter:
			text: "ʘ"
		windmill:
			text: "¥"
		block:
			text: "◼"
		axe:
			text: "}"
		hammer:
			text: "}"
		pick:
			text: "}"
		sword:
			text: "/"
		armor:
			text: "]"
]
