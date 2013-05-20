services = angular.module "app.services", []

services.factory "server", ["$rootScope", "$log", ($rootScope, $log) ->
	move = (x, y) ->
		$log.log "server: move invoked [x="+x+" y="+y+"]";
		
		player = $rootScope.player;
		worldLen = $rootScope.worldLen;
		chunkLen = $rootScope.chunkLen;
		
		tileAt = (x, y) ->
			cx = Math.floor(x / chunkLen);
			cy = Math.floor(y / chunkLen);
			tx = (x + chunkLen) % chunkLen;
			ty = (y + chunkLen) % chunkLen;
			chunk = $rootScope.chunks[cx*worldLen+cy]
			if chunk?
				chunk.tiles[tx*chunkLen+ty];
			else
				null
		prevTile = tileAt(player.x, player.y)
		nextTile = tileAt(player.x+x, player.y+y);
		if prevTile? and nextTile? and !(nextTile.entity?)
			nextTile.entity = prevTile.entity;
			prevTile.entity = null;
			$rootScope.player.x += x;
			$rootScope.player.y += y;
			$rootScope.$apply()		# FIXME: slow!
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

