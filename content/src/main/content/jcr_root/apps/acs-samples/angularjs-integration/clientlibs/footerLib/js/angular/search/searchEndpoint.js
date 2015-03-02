/*global sampleModule*/
/*
 * Endpoints are AngularJS services used to contain logic for connecting to 3rd party APIs, Sling, etc.
 * The main idea is to separate the implementation details of the AJAX requests from the business logic in the models and the presentation logic in controllers.
 */
sampleModule.factory('searchEndpoint', ['$q', '$timeout', function($q, $timeout) {

	var endpoint = {}, 
	mockSearchResults = [
	                         {name:'Brown Circle', description:'result1 description'},
	                         {name:'Red Triangle', description:'result2 description'},
	                         {name:'Orange Square', description:'result3 description'}
	                         ];

	endpoint.performSearch = function(searchTerm) {
		var deferred = $q.defer();

		/*
		 * At this point, we would make a call to some sort of back end service to do the search using the $http service.
		 * On success, we would call deferred.resolve with the results.
		 * On failure, we would call deferred.reject with an error message.
		 * To simulate the asynchronous nature of this, I have used a short timeout before searching the mock results
		 * Timeouts can be used in production code, but usage should be minimized to avoid performance issues.
		 */

		$timeout(function() {
			var filteredResults = mockSearchResults.filter(function(value, index, array1) {
				return (value.name.indexOf(searchTerm) > 0);
			});
			
			deferred.resolve(filteredResults);
		}, 100);

		return deferred.promise;
	};

	return endpoint;
}]);