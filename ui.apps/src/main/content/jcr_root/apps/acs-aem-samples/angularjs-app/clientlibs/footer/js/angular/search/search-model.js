/*global console, sampleModule*/

/*
 * Models are the main container area for business logic in the application stack.
 * Additionally, they are singletons which will maintain their state when shared between controllers.
 */
sampleModule.factory('searchModel', ['searchEndpoint', function(searchEndpoint) {
	
	var model = {};
	
	model.data = {
			searchTerm:"",
			searchResults:[],
			selectedResult:{}
	};
	
	function updateSearchResults(results) {
		model.data.searchResults = results;
	}
	
	function handleError(error) {
		console.log(error);
	}
	
	model.performSearch = function(searchTerm) {
		searchEndpoint.performSearch(searchTerm).then(updateSearchResults, handleError);
	};
	
	return model;
	
}]);