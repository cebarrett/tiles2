window.app = angular.module "app", ["app.filters", "app.services", "app.directives", "app.controllers"]
window.onload = () ->
	angular.bootstrap(document, ['app'])