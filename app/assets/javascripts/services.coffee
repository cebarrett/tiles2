services = angular.module "app.services", []

services.factory "server", ["$rootScope", "$log", ($rootScope, $log) ->
	move = (x, y) ->
		$log.log "server: move invoked [x="+x+" y="+y+"]";
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
