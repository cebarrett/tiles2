controllers = angular.module "app.controllers", ["app.services", "app.directives"]

controllers.controller "AppCtrl", ["$scope", "net", "chunkManager", "chunkQueue", ($scope, net, chunkManager, chunkQueue) ->
	
	window.scope = $scope
	
	$('body').on('selectstart', () -> false)
	$('body').on('select', () -> false)
	
	# XXX: should send the server side ones in init
	$scope.tileSizePx = 30;	# also defined in LESS
	$scope.chunkLen = 16;	# also defined in server side and in LESS
	$scope.worldLen = 64;	# also defined in server side

	$scope.loadChunk = (chunk)  ->
		chunkQueue.loadChunk(chunk)
		
	$scope.unloadChunk = (cx, cy) ->
		chunkQueue.unloadChunk(cx, cy)

	# initialize the chunk dom element pool
	chunkManager.init $scope

	# connect to the server
	net.connect $scope

	$scope.north = -> $scope.openGui(0,1)  || net.north()
	$scope.east  = -> $scope.openGui(1,0)  || net.east()
	$scope.south = -> $scope.openGui(0,-1) || net.south()
	$scope.west  = -> $scope.openGui(-1,0) || net.west()
		
	$scope.chunkOffset = (n) ->
		Math.floor(n / $scope.chunkLen)

	$scope.chunkCoordsAt = (x, y) ->
		{cx: $scope.chunkOffset(x), cy: $scope.chunkOffset(y)}

	$scope.chunkAt = (x, y) ->
		cc = $scope.chunkCoordsAt(x, y)
		chunkManager.chunkAt(cc.cx, cc.cy)

	$scope.tileOffset = (n) ->
		chunkLen = $scope.chunkLen
		((n % chunkLen) + chunkLen) % chunkLen

	$scope.tileCoordsAt = (x, y) ->
		{tx: $scope.tileOffset(x), ty: $scope.tileOffset(y)}

	$scope.tileAt = (x, y) ->
		chunk = $scope.chunkAt(x, y)
		if chunk?
			tc = $scope.tileCoordsAt(x, y)
			chunk.tiles[tc.tx][tc.ty]

	# tile click callback
	$scope.place = (x, y) ->
		net.place(x, y, $scope.selectedItemIndex)

	# select active item callback
	$scope.selectItem = (index) ->
		net.selectItem(index)

	# open a crafting gui for the tile dx,dy from the player.
	# return true if one was opened, false otherwise.
	$scope.openGui = (dx, dy) ->
		if !scope.player? then return false
		tile = $scope.tileAt($scope.player.x+dx, $scope.player.y+dy)
		if (!tile?)
			console.warn "no tile at "+($scope.player.x+dx)+" "+ ($scope.player.y+dy)
		if (tile? && tile.entity?)
			if (_($scope.crafts).find({kind: tile.entity.kind})?)
				stack = $scope.item()
				if (stack? && (stack.item.kind == "hammer"))
					return false
				else
					$scope.gui = tile.entity.kind
					$scope.$apply()
					return true
		else return false

	$scope.selectRecipe = (craft, index) ->
		net.craft(craft, index)

	$scope.closeGui = () ->
		delete $scope.gui

	$scope.item = ()->
		index = $scope.player.selected
		if (index?) then $scope.player.inventory[index]
];
