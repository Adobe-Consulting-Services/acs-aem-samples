<%--
  ~ #%L
  ~ ACS AEM Samples
  ~ %%
  ~ Copyright (C) 2015 Adobe
  ~ %%
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~ #L%
  --%>
<%@page session="false" import="
                  com.adobe.granite.ui.components.ds.DataSource,
                  com.adobe.granite.ui.components.ds.EmptyDataSource,
                  com.adobe.granite.ui.components.ds.SimpleDataSource,
                  com.adobe.granite.ui.components.ds.ValueMapResource,
                  org.apache.sling.api.resource.Resource,
                  org.apache.sling.api.resource.ResourceMetadata,
                  org.apache.sling.api.resource.ValueMap,
                  org.apache.sling.api.wrappers.ValueMapDecorator,
                  java.util.ArrayList,
                  java.util.HashMap,
                  java.util.Iterator,
                  java.util.List,
                  javax.jcr.query.Query"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %><%
%><cq:defineObjects/><%
    // Set a fallback DataSource to be an Empty DataSource
    request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());

    // The GraniteUI Widget should define the datasource beneath it using the nodeName "datasource"
    Resource datasource = resource.getChild("datasource");

    // Configuration data can be passed into this Datasource impl by way of the datasource node.
    ValueMap dsProperties = datasource.getValueMap();
    String path = dsProperties.get("path", String.class);

    // Collect whatever any data you want to expose in the datasource. This is a simple example selecting
    // Pages under a path specified on the datasource node
    Iterator<Resource> resources =
            resourceResolver.findResources("SELECT * FROM [cq:PageContent] WHERE ISDESCENDANTNODE([" + path + "])",
            Query.JCR_SQL2);

    if (resources.hasNext()) {
        // Create a list to capture the items to be added to the data source; This can be sorted, etc.
        List<Resource> fakeResourceList = new ArrayList<Resource>();

        while (resources.hasNext()) {
            // For each item to add to the datasource
            Resource item = resources.next();
            // Create a ValueMap which will represent an item to display via the datasource
            ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
            // Put the value for this item
            vm.put("value", item.getPath());
            // Get the label to display tho the Author that represents this item
            vm.put("text", item.getValueMap().get("jcr:title", "Missing title"));
            // Some "magic". Create a list of "fake" resources and provide the populated ValueMap
            fakeResourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), "nt:unstructured", vm));
        }

        // Create a DataSource from the items
        DataSource ds = new SimpleDataSource(fakeResourceList.iterator());
        // Set the this DataSource to request
        request.setAttribute(DataSource.class.getName(), ds);
    } else {
        // If no data for the datasource can be found, default to the EmptyDataSource set on the request above
    }
%>