directives = angular.module "app.directives", ["app.services"]
directives.directive "appControls", ["$document", "playerService", ($document, playerService) ->
	(scope, elm, attrs) ->
		elm? and elm.length>0 and elm[0].addEventListener 'keydown', (e) ->
			# first check that user isn't typing in an input
			return undefined if $document[0].querySelectorAll(":focus").length > 0
			# now move
			playerService.north() if e.keyCode is 87	# w
			playerService.south() if e.keyCode is 83	# s
			playerService.west() if e.keyCode is 65		# a
			playerService.east() if e.keyCode is 68		# d
			playerService.use() if e.keyCode is 32		# space
];
