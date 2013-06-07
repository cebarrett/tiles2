directives = angular.module "app.directives", []

directives.directive "appControls", [ () ->
	(scope, elm, attrs) ->
		step = () ->
			# TODO: use the time delta to smooth and slow down keyboard movement
			window.requestAnimationFrame(step)
		step()

		$('body').on 'mousewheel', (e) ->
			# TODO: scroll item selection
			delta = e.originalEvent.wheelDeltaY
			console.log(delta)
			return

		down = {}
		$(document).on "keydown", (e) ->
			# first check that user isn't typing in an input
			# FIXME: tab still prevents input
			return if $("input:focus, textarea:focus, button:focus").size() > 0
			# now move
			scope.north() if e.keyCode is 87 or e.keyCode is 38
			scope.south() if e.keyCode is 83 or e.keyCode is 40
			scope.west()  if e.keyCode is 65 or e.keyCode is 37
			scope.east()  if e.keyCode is 68 or e.keyCode is 39
			(scope.guiSelect(0); scope.$apply()) if scope.guiOptions? and e.keyCode is 27 #esc
		$(document).on "keyup", (e) ->
			down[e.keyCode] = false
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
				tile.entity.id
			else
				tile.terrain.id
		render = tileRender[id];
		$tile.html "&#"+render.text.charCodeAt(0)+";"
		$tile.css {color: render.color}

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
