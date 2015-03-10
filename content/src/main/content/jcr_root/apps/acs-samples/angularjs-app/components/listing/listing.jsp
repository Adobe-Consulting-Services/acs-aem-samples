<%@page session="false"%>
<%@include file="/libs/foundation/global.jsp"%>

<%--
A search results list component used on the results page.
data-ng-repeat iterates over the searchResults that have been assigned to scope in the controller and outputs a div for each of them.
The name and description are bound to each of the searchResults.
data-ng-click registers a handler for a button click on each of the elements and assigns it to a function in scope.
 --%>

Search Results:

<div id="searchResults" data-ng-repeat="result in searchData.searchResults">
	<div class="searchResultName">{{result.name}}</div>
	<div class="searchResultDescription">{{result.description}}</div>
	<button class="searchResultButton" data-ng-click="selectResult(result)">Select</button>
</div>