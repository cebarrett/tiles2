directives = angular.module "app.directives", []

directives.directive "appControls", ["$document", "$parse", ($document, $parse) ->
	(scope, elm, attrs) ->
		# XXX: there must be a better way to do this
		north = $parse("north()")
		east = $parse("east()")
		west = $parse("west()")
		south = $parse("south()")
		use = $parse("use()")
		$document[0].body.addEventListener 'keydown', (e) ->
			# first check that user isn't typing in an input
			# FIXME: tab still prevents input
			return if $document[0].querySelectorAll("input:focus, textarea:focus").length > 0
			# now move
			scope.$eval north if e.keyCode is 87	# w
			scope.$eval south if e.keyCode is 83	# s
			scope.$eval west if e.keyCode is 65		# a
			scope.$eval east if e.keyCode is 68		# d
			scope.$eval use if e.keyCode is 32		# space
];

directives.directive "world", [->
	(scope, elm, attr) ->
		# doesn't do much yet
		elm.addClass "world"
		renderPlayerMove = () ->
			elm.css {
				# FIXME: hardcoded base offsets
				top: (300+scope.player.y*scope.tileSizePx)+"px"
				left: (330-scope.player.x*scope.tileSizePx)+"px"
			}
		scope.$watch("player.x", renderPlayerMove)
		scope.$watch("player.y", renderPlayerMove)
		renderPlayerMove()
];

directives.directive "chunk", [->
	(scope, elm, attr) ->
		elm.addClass "chunk"
		elm.css "top", -((1+scope.chunk.cy)*scope.tileSizePx*scope.chunkLen)+"px"
		elm.css "left", ((1+scope.chunk.cx)*scope.tileSizePx*scope.chunkLen)+"px"
];

directives.directive "tile", [->
	(scope, elm, attr) ->
		elm.addClass "tile"
		elm.css "top", -((1-scope.chunkLen+scope.tile.ty)*scope.tileSizePx)+"px"
		elm.css "left", ((scope.tile.tx)*scope.tileSizePx)+"px"
		setTileImage = (x, y) ->	
			elm.css "background-position-x", (-scope.tileSizePx*x)+"px"
			elm.css "background-position-y", (-scope.tileSizePx*y)+"px"
		renderTile = ->
			if scope.tile.entity?
				switch scope.tile.entity.type
					when "player" then setTileImage 0,4
					when "tree" then setTileImage 5,0
			else
				switch scope.tile.terrain.type
					when "water" then setTileImage 7,15
					when "dirt" then setTileImage 7,0
					else setTileImage 0,0 # unknown terrain type
		scope.$watch("tile.entity", renderTile);
		renderTile();
];
