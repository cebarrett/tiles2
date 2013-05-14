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
