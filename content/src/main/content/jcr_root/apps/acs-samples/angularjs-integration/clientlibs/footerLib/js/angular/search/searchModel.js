/*global console, sampleModule*/
sampleModule.factory('searchModel', ['searchEndpoint', function(searchEndpoint) {
	
	var model = {};
	
	model.data = {
			searchTerm:"",
			searchResults:[]
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