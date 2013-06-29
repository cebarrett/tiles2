directives = angular.module "app.directives", []

directives.directive "appControls", [ () ->
	# TODO: hold shift to place blocks
	# TODO: Q and E to cycle items
	# TODO: shift-Q and shift-E to rearrange items
	# TODO: mouse wheel to cycle items
	# TODO: space de-selects item
	# FIXME: tab still prevents input
	(scope, elm, attrs) ->
		speed = 5;	# moves per second

		$('body').on 'mousewheel', (e) ->
			delta = e.originalEvent.wheelDeltaY
			console.log(delta)
			return

		move = {north: false, south: false, east: false, west: false}
		moveCount = {north: 0, south: 0, east: 0, west: 0}

		doMovement = (dir) ->
			# FIXME: can move 2 directions at once, so faster diagonally
			# to fix, only move 1 direction, the most recently selected
			if ((false == move[dir]?) or (false == move[dir]))
				return false
			if ((false == moveCount[dir]?) or (moveCount[dir] == 0))
				scope[dir]()
				moveCount[dir] = 1
				return true
			timestamp = new Date().getTime()
			delta = timestamp - move[dir]
			newMoveCount = Math.floor(delta * speed / 1000)
			if (newMoveCount > moveCount[dir])
				scope[dir]()
				moveCount[dir]++
				return true
			else
				return false

		step = () ->
			if !doMovement("north")
				if !doMovement("east")
					if !doMovement("south")
						doMovement("west")
			window.requestAnimationFrame(step)
		step()

		$(document).on "keydown", (e) ->
			# first check that user isn't typing in an input
			return if $("input:focus, textarea:focus, button:focus").size() > 0
			
			# now move
			if (e.keyCode is 87 or e.keyCode is 38) and !move.north
				move.north = new Date().getTime()
			if (e.keyCode is 83 or e.keyCode is 40) and !move.south
				move.south = new Date().getTime()
			if (e.keyCode is 65 or e.keyCode is 37) and !move.west
				move.west = new Date().getTime()
			if (e.keyCode is 68 or e.keyCode is 39) and !move.east
				move.east = new Date().getTime()
			if e.keyCode is 27 #esc
				scope.closeGui()
				scope.$apply()

		$(document).on "keyup", (e) ->
			if e.keyCode is 87 or e.keyCode is 38
				move.north = false
				moveCount.north = 0
			if e.keyCode is 83 or e.keyCode is 40
				move.south = false
				moveCount.south = 0
			if e.keyCode is 65 or e.keyCode is 37
				move.west = false
				moveCount.west = 0
			if e.keyCode is 68 or e.keyCode is 39
				move.east = false
				moveCount.east = 0
];

directives.directive "world", [ "$window", ($window) ->
	contentElm = $ "#content"
	(scope, elm, attr) ->
		# doesn't do much yet
		elm.addClass "world"
		renderPlayerMove = () ->
			if scope.player? then elm.css {
				top:  (contentElm.height()/2+scope.player.y*scope.tileSizePx)+"px"
				left: (contentElm.width()/2-scope.player.x*scope.tileSizePx)+"px"
			}
		scope.$watch "player.x", renderPlayerMove
		scope.$watch "player.y", renderPlayerMove
		
		$window.addEventListener "resize", renderPlayerMove, false
		renderPlayerMove()
];

#
# Renders a chunk and all of its tiles.
# (No longer uses a directive for tiles because ngRepeatWatch
# is slow with lots of tiles)
#
directives.directive "chunk", [ "tileRender", (tileRender) ->
	updateTile = (tile, $tile) ->
		id =
			if tile.entity?
				tile.entity.kind
			else
				tile.terrain.id
		render = tileRender[id];
		$tile.html "&#"+render.text.charCodeAt(0)+";"
		renderColor = do ->
			if (tile.entity? and tile.entity.material? and tile.entity.material.color?)
				tile.entity.material.color
			else
				render.color
		$tile.css {color: renderColor}

	(scope, elm, attr) ->
		elm.addClass "chunk"
		elm.css "top", -((1+scope.chunk.cy)*scope.tileSizePx*scope.chunkLen)+"px"
		elm.css "left", ((scope.chunk.cx)*scope.tileSizePx*scope.chunkLen)+"px"
		_(scope.chunk.tiles).each (tileCol, tx) ->
			$tileCol = $('<div class="tile-column">')
			$tileCol.css "left", (tx*scope.tileSizePx)+"px"
			_(tileCol).each (tile, ty) ->
				x = scope.chunk.cx * scope.chunkLen + tx
				y = scope.chunk.cy * scope.chunkLen + ty
				$tile = $('<div class="tile '+x+'_'+y+'">')
				$tile.css "top", -((1-scope.chunkLen+ty)*scope.tileSizePx)+"px"
				# FIXME: don't bind event listeners to each tile
				$tile.on 'selectstart', () -> false
				$tile.on 'click', (e) -> 
					scope.place(x, y) if scope.place?
				$tile.on 'mouseover', (e) ->
					scope.place(x, y) if scope.place? and e.which==1
				updateTile(tile, $tile)
				$tileCol.append($tile)
			elm.append($tileCol[0])
		# these events are broadcast by the sub service when a tile changes
		scope.$on 'tileChange', (something, x, y, tile) ->
			$tile = $('.'+x+'_'+y)
			updateTile(tile, $tile)
];

directives.directive "item", [ () ->
	return (scope, elm, attr) ->
		$('body').on('selectstart', () -> false)
		$('body').on('select', () -> false)
];
