/*global sampleModule*/
sampleModule.factory('routingService', ['$location', function($location) {

	var service = {};
	
	service.goToHome = function() {
		$location.path('home');
	};
	
	service.goToSearch = function() {
		$location.path('search');
	};

	service.goToCheckout = function() {
		$location.path('checkout');
	};
	
	return service;
	
}]);