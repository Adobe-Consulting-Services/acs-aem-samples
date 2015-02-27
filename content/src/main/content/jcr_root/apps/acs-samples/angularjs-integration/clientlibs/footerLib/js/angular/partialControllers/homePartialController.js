/*global sampleModule*/
sampleModule.controller('homePartialController', ['$scope', 'searchModel', 'routingService', function($scope, searchModel, routingService) {
	$scope.searchTerm = searchModel.data.searchTerm;
	
	$scope.submitSearch = function() {
		searchModel.performSearch($scope.searchTerm);
		routingService.goToSearch();
	};
}]);
