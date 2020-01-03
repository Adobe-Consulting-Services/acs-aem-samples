<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>
<%--
The container page extends base-page, which sets up the Angular module and base controller.
When the application is loaded, Angular routing will insert a page partial into the div tag below based on the URL path.
data-ng-view denotes this div as being used by routing and the content to be loaded in configured in main.js.
 --%>
<div id="partialContainer" data-ng-view>
</div>