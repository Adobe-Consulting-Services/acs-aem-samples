<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>

<%--
A search box component used on the home page.
data-ng-model binds the value of the text box input to searchTerm on $scope.  searchTerm is assigned to a model value in the partial controller.
data-ng-click sets a function to use when the button is clicked.  Again, submitSearch() is assigned by the controller to a function in the model.
Note that the ctaLabel is configurable by the author in the dialog.
 --%>

<div id="searchBox">
	<input id="searchBoxInput" type="text" data-ng-model="searchData.searchTerm">
	<button type="submit" id="searchBoxCTAButton" data-ng-click="submitSearch()">${properties.ctaLabel}</button>
</div>
