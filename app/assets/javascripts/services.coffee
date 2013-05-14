services = angular.module "app.services", []

services.factory "server", ["$rootScope", "$log", ($rootScope, $log) ->
	move = (x, y) ->
		$log.log "server: move invoked [x="+x+" y="+y+"]";
		
		player = $rootScope.player;
		chunkLen = $rootScope.chunkLen;
		
		tileAt = (x, y) ->
			cx = x / chunkLen;
			cy = y / chunkLen;
			tx = (x + chunkLen) % chunkLen;
			ty = (y + chunkLen) % chunkLen;
			chunk = $rootScope.chunks[0];	# FIXME: doesn't work
			tile = chunk.tiles[tx*chunkLen+ty];
		prevTile = tileAt(player.x, player.y)
		nextTile = tileAt(player.x+x, player.y+y);
		if prevTile? and nextTile? and !(nextTile.entity?)
			nextTile.entity = prevTile.entity;
			prevTile.entity = null;
			$rootScope.player.x += x;
			$rootScope.player.y += y;
	use = ->
		$log.log "server: use invoked"
	{
		north: () -> move 0,1
		south: () -> move 0,-1
		east: () -> move 1,0
		west: () -> move -1,0
		use: () -> use()
	}
];
