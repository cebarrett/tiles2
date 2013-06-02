directives = angular.module "app.directives", []

directives.directive "appControls", ["$document", "$parse", ($document, $parse) ->
	(scope, elm, attrs) ->
		# there must be a better way to do this
		north = $parse("north()")
		east = $parse("east()")
		west = $parse("west()")
		south = $parse("south()")
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

directives.directive "chunk", [->
	(scope, elm, attr) ->
		elm.addClass "chunk"
		elm.css "top", -((1+scope.chunk.cy)*scope.tileSizePx*scope.chunkLen)+"px"
		elm.css "left", ((scope.chunk.cx)*scope.tileSizePx*scope.chunkLen)+"px"
];

directives.directive "tileColumn", [->
	(scope, elm, attr) ->
		elm.addClass "tile-column"
		elm.css "left", (scope.$index*scope.tileSizePx)+"px"
];

directives.directive "tile", [ () ->
	# FIXME: put this in its own service
	tileRender = {
		player:
			text: "@"
			color: "white"
		tree:
			text: "♠"
			color: "#00BB00"
		water:
			text: "≈"
			color: "#6666FF"
		dirt:
			text: "."
			color: "#A06030"
		workbench:
			text: "⩦"
			color: "#BB7722"
	}

	return (scope, elm, attr) ->
		elm.addClass "tile"
		elm.bind 'selectstart', () -> false
		elm.css "top", -((1-scope.chunkLen+scope.$index)*scope.tileSizePx)+"px"
		updateTile = ->
			id =
				if scope.tile.entity?
					scope.tile.entity.id
				else
					scope.tile.terrain.id
			render = tileRender[id];
			elm[0].innerHTML = "&#"+render.text.charCodeAt(0)+";"
			elm.css {color: render.color}
		scope.$watch("tile.entity", updateTile);
		scope.$watch("tile.terrain", updateTile);
		updateTile();
];
