<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>

<div id="searchBox">
	<input id="searchBoxInput" type="text" data-ng-model="searchTerm">
	<button type="submit" id="searchBoxCTAButton" data-ng-click="submitSearch()">${properties.ctaLabel}</button>
</div>
