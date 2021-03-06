directives = angular.module "app.directives", []

directives.directive "appControls", [ "renderLoop", (renderLoop) ->
	
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
			if _([87, 38, 83, 40, 65, 37, 68, 39]).contains(e.keyCode) # WASD and arrows
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
			if e.keyCode is 27 # esc
				scope.closeGui()
				scope.$apply()
			if _([81, 69, 32]).contains(e.keyCode) # Q E space
				# first check that user isn't typing in an input
				return if $("input:focus, textarea:focus").size() > 0
				len = scope.player.inventory.length
				sel = scope.player.selected
				alt = e.shiftKey
				if e.keyCode == 81
					if sel?
						if alt
							scope.swapItemUp()
						else
							scope.selectItem (sel-1+len)%len
					else
						scope.selectItem len-1
				if e.keyCode == 69
					if sel?
						if alt
							scope.swapItemDown()
						else
							scope.selectItem (sel+1)%len
					else
						scope.selectItem 0
				if e.keyCode == 32
					scope.selectItem()
		
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
		
		renderLoop.addCallback () ->
			# if any movement key is selected and the total number of moves
			# since started moving is less than the total moves allowed
			# since started moving, move in the most recently selected direction.
			times  = _(dirMoveTimes).values().filter()
			counts = _(dirMoveCounts).values().filter()
			isMoving = times.size() > 0
			if isMoving
				totalMovesAllowed = (new Date().getTime() - moveTime)/1000.0 * movesPerSecond
				if (moveCount < totalMovesAllowed)
					newestMoveTime = times.reduce ((a, v) -> Math.max(a, v)), 0
					newestDir = _(dirMoveTimes).findKey (v) -> v == newestMoveTime
					scope[newestDir]()
					dirMoveCounts[newestDir]++
					moveCount++;
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

directives.directive "sky", [ () ->
	duration = 0.15 # percentage of day taken by a sunrise or sunset
	sunrise =
		start:  6 - (24 * (duration / 2))
		end:    6 + (24 * (duration / 2))
	sunset =
		start: 18 - (18 * (duration / 2))
		end:   18 + (18 * (duration / 2))
	day =
		hue: 50
		luminosity: 93
	night = 
		hue: 250
		luminosity: 44
	hue = (time) ->
		# hue goes down during sunset and up during sunrise
		if (time >= sunrise.end && time < sunset.start)
			day.hue
		else if (time >= sunset.start && time < sunset.end)
			dist = (time - sunset.start) / (sunset.end - sunset.start)
			n = day.hue - (dist * (day.hue + (360 - night.hue)))
			(n+360)%360
		else if (time >= sunset.end || time < sunrise.start)
			night.hue
		else if (time >= sunrise.start && time < sunrise.end)
			dist = (time - sunrise.start) / (sunrise.end - sunrise.start)
			n = night.hue + (dist * (day.hue + (360 - night.hue)))
			(n+360)%360
		else
			console.error("Couldn't determine hue for time " + time)
			day.hue
	saturation = (time) -> 80
	luminosity = (time) ->
		# luminosity goes down during sunset and up during sunrise
		if (time >= sunrise.end && time < sunset.start)
			day.luminosity
		else if (time >= sunset.start && time < sunset.end)
			dist = (time - sunset.start) / (sunset.end - sunset.start)
			day.luminosity - dist * (day.luminosity - night.luminosity)
		else if (time >= sunset.end || time < sunrise.start)
			night.luminosity
		else if (time >= sunrise.start && time < sunrise.end)
			dist = (time - sunrise.start) / (sunrise.end - sunrise.start)
			night.luminosity + dist * (day.luminosity - night.luminosity)
		else
			console.error("Couldn't determine luminosity for time " + time)
			day.luminosity
	alpha = (time) -> 0.13
	renderTimeOfDay = (scope, elm) ->
		bgcolor = "black"
		if (scope.timeStr?)
			time = scope.time()
			bgcolor = "hsla("+hue(time)+","+saturation(time)+"%,"+luminosity(time)+"%,"+alpha(time)+")"
		elm.css "background-color", bgcolor
	(scope, elm, attr) ->
		scope.$watch "timeStr", () -> renderTimeOfDay(scope, elm)
]

