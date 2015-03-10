/*global sampleModule*/

/*
 * Partial controllers are attached to page partials when loaded through Angular routing.
 * They are most useful when you want to configure a controller for a partial itself or for all components on a specific partial to share.
 */
sampleModule.controller('homePartialController', ['$scope', 'searchModel', 'routingService', function($scope, searchModel, routingService) {
	$scope.searchData = searchModel.data;
	
	$scope.submitSearch = function() {
		searchModel.performSearch($scope.searchData.searchTerm);
		routingService.goToSearch();
	};
}]);
