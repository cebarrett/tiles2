directives = angular.module "app.directives", []

directives.directive "appControls", ["$document", "$parse", ($document, $parse) ->
	(scope, elm, attrs) ->
		# there must be a better way to do this
		north = $parse("north()")
		east = $parse("east()")
		west = $parse("west()")
		south = $parse("south()")
		$('body').on 'mousewheel', (e) ->
			delta = e.originalEvent.wheelDeltaY
			return	# TODO: scroll selected item
		
		$document[0].body.addEventListener 'keydown', (e) ->
			# first check that user isn't typing in an input
			# FIXME: tab still prevents input
			return if $document[0].querySelectorAll("input:focus, textarea:focus").length > 0
			# now move
			scope.$eval north if e.keyCode is 87	# w
			scope.$eval south if e.keyCode is 83	# s
			scope.$eval west if e.keyCode is 65		# a
			scope.$eval east if e.keyCode is 68		# d
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
		# TODO: watch/listen to something for events and update the tile
		scope.$on 'tileChange', (something, x, y, tile) ->
			if (tile.entity? == false)
				console.log "tileChange, removing entity"
			# FIXME: slow selector, use a class instead of data attrs
			$tile = $('.'+x+'_'+y)
			updateTile(tile, $tile)
];

directives.directive "item", [ () ->
	return (scope, elm, attr) ->
		$('body').on('selectstart', () -> false)
		$('body').on('select', () -> false)
];
