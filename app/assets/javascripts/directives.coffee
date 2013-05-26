directives = angular.module "app.directives", []

directives.directive "appControls", ["$document", "$parse", ($document, $parse) ->
	(scope, elm, attrs) ->
		# XXX: there must be a better way to do this
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

directives.directive "world", ["$window", ($window) ->
	(scope, elm, attr) ->
		# doesn't do much yet
		elm.addClass "world"
		renderPlayerMove = () ->

			if scope.player? then elm.css {
				top:  ($window.innerHeight/2+scope.player.y*scope.tileSizePx)+"px"
				left: ($window.innerWidth /2-scope.player.x*scope.tileSizePx)+"px"
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

directives.directive "tile", [ () ->
	# FIXME: use a filter
	tileRender = {
		player:
			text: "@"
			color: "white"
		hydra:
			text: "∭"
			color: "#6F6"
		tree:
			text: "♠"
			color: "green"
		water:
			text: "≈"
			color: "#4444CC"
		dirt:
			text: "."
			color: "brown"
	}

	return (scope, elm, attr) ->
		elm.addClass "tile"
		elm.bind 'selectstart', () -> false
		elm.css "top", -((1-scope.chunkLen+scope.tile.ty)*scope.tileSizePx)+"px"
		elm.css "left", ((scope.tile.tx)*scope.tileSizePx)+"px"
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
