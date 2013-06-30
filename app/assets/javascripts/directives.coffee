directives = angular.module "app.directives", []

directives.directive "appControls", [ () ->

	# TODO: hold shift to place blocks
	# TODO: Q and E to cycle items
	# TODO: shift-Q and shift-E to rearrange items
	# TODO: space de-selects item
	# FIXME: tab still prevents input
	
	return (scope, elm, attrs) ->
		dirMoveTimes  = {north: null, south: null, east: null, west: null}
		dirMoveCounts = {north: null, south: null, east: null, west: null}
		moveTime  = null
		moveCount = null
		movesPerSecond = 5
		
		$(document).on "keydown", (e) ->
			if _([87, 38, 83, 40, 65, 37, 68, 39]).contains(e.keyCode)
				# first check that user isn't typing in an input
				return if $("input:focus, textarea:focus").size() > 0
				# now record which direction was pressed
				if (e.keyCode is 87 or e.keyCode is 38) and !dirMoveTimes.north
					dirMoveTimes.north  = new Date().getTime()
					dirMoveCounts.north = 0
				if (e.keyCode is 83 or e.keyCode is 40) and !dirMoveTimes.south
					dirMoveTimes.south  = new Date().getTime()
					dirMoveCounts.south = 0
				if (e.keyCode is 65 or e.keyCode is 37) and !dirMoveTimes.west
					dirMoveTimes.west  = new Date().getTime()
					dirMoveCounts.west = 0
				if (e.keyCode is 68 or e.keyCode is 39) and !dirMoveTimes.east
					dirMoveTimes.east  = new Date().getTime()
					dirMoveCounts.east = 0
				if (!moveTime?)
					moveTime = new Date().getTime()
					moveCount = 0
			if e.keyCode is 27
				scope.closeGui()
				scope.$apply()
		
		$(document).on "keyup", (e) ->
			if e.keyCode is 87 or e.keyCode is 38
				dirMoveTimes.north  = null
				dirMoveCounts.north = null
			if e.keyCode is 83 or e.keyCode is 40
				dirMoveTimes.south  = null
				dirMoveCounts.south = null
			if e.keyCode is 65 or e.keyCode is 37
				dirMoveTimes.west  = null
				dirMoveCounts.west = null
			if e.keyCode is 68 or e.keyCode is 39
				dirMoveTimes.east  = null
				dirMoveCounts.east = null
			if _(dirMoveTimes).values().filter().size() == 0
				moveTime = null
				moveCount = null
		
		step = () ->
			# if any movement key is selected and the total number of moves
			# since started moving is less than the total moves allowed
			# since started moving, move in the most recently selected direction.
			times  = _(dirMoveTimes).values().filter()
			counts = _(dirMoveCounts).values().filter()
			isMoving = times.size() > 0
			if isMoving
				totalMovesAllowed = (new Date().getTime() - moveTime)/1000.0 * movesPerSecond
				console.log(moveCount + " ~ " + Math.floor(totalMovesAllowed))
				if (moveCount < totalMovesAllowed)
					newestMoveTime = times.reduce ((a, v) -> Math.max(a, v)), 0
					newestDir = _(dirMoveTimes).findKey (v) -> v == newestMoveTime
					scope[newestDir]()
					dirMoveCounts[newestDir]++
					moveCount++;
			window.requestAnimationFrame(step)
		step()
];

# currently unused
# TODO: mouse wheel to cycle items
directives.directive "mouseWheelControls", [ () ->
	(scope, elm, attr) ->
		$('body').on 'mousewheel', (e) ->
			delta = e.originalEvent.wheelDeltaY
			console.log(delta)
			return
]

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
