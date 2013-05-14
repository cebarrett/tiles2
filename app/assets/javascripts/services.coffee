services = angular.module "app.services", []

services.factory "playerService", ["$log", ($log) ->
	# temporary method
	move = (x, y) ->
		$log.log "playerService: move invoked [x="+x+" y="+y+"]";
	use = ->
		$log.log "playerService: use invoked"
	{
		north: () -> move 0,1
		south: () -> move 0,-1
		east: () -> move 1,0
		west: () -> move -1,0
		use: () -> use()
	}
];
