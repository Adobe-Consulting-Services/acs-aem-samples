/*global sampleModule*/

/*
 * An Angular service to abstract path names from the rest of the application.
 * It is a good idea to use something like this so that we don't have string literals repeated all over the code base.
 */
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