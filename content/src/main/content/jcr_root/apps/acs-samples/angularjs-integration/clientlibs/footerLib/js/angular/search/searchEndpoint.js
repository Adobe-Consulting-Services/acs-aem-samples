/*global sampleModule*/
sampleModule.factory('searchEndpoint', ['$q', '$timeout', function($q, $timeout) {

	var endpoint = {}, 
	mockSearchResults = [
	                         {name:'result1', description:'result1 description'},
	                         {name:'result2', description:'result2 description'},
	                         {name:'result3', description:'result3 description'}
	                         ];

	endpoint.performSearch = function(searchTerm) {
		var deferred = $q.defer();

		/*
		 * At this point, we would make a call to some sort of back end service to do the search using the $http service.
		 * On success, we would call deferred.resolve with the results.
		 * On failure, we would call deferred.reject with an error message.
		 * To simulate the asynchronous nature of this, I have used a short timeout before returning some mock results
		 */

		$timeout(function() {
			deferred.resolve(mockSearchResults);
		}, 100);

		return deferred.promise;
	};

	return endpoint;
}]);