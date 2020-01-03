<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>
<%--
The partial-page template is used to create the partials that will be loaded into the container-page.
In theory, we could use instances of base-page, but in my experience, there is value in having a separate template here.
Oftentimes, you may want some sort of different behavior when loading content through routing rather than through a standalone page.
 --%>
<div id="bodyContent">
	<cq:include path="mainContent" resourceType="foundation/components/parsys"/>
</div>