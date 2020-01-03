<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>
<%--
We assign the Angular module here as well as our base controller.
This base page component is never used directly, but rather is extended by the container-page and partial-page components.
 --%>
<body data-ng-app="angularjs-sample"  data-ng-controller="baseController">
<cq:include script="header.jsp"/>
<cq:include script="content.jsp"/>
<cq:include script="footer.jsp" />
</body>