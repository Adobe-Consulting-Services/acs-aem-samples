/*
 * #%L
 * ACS AEM Samples
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.samples.resourceproviders.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(
        label = "ACS AEM Samples - Sling Resource Provider",
        description = "Sample Sling Resource Provider",
        immediate = true
)
@Properties({
        @Property(
                label = "Root paths",
                description = "Root paths this Sling Resource Provider will respond to",
                name = ResourceProvider.ROOTS,
                value = {"/content/mount/samples"})
})
@Service
public class SampleResourceProvider implements ResourceProvider {

    private List<String> roots;

    @Override
    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        // For this example the Request is not taken into consideration when evaluating
        // the Resource request, so we just call getResource(rr, path)

        // Remember, since this is a Synthetic resource there are no ACLs applied to this
        // resource. If you would like to restrict access, it must be done programmatically by checking
        // the ResourceResolver's user.
        return getResource(resourceResolver, path);
    }

    @Override
    public Resource getResource(ResourceResolver resourceResolver, String path) {
        // Make getResource() return as fast as possible!
        // Return null early if getResource() cannot/should not process the resource request

        // If path is a root, return a Synthetic Folder
        // This could be any "type" of SyntheticResource
        if (isRoot(path)) {
            return new SyntheticResource(resourceResolver, path, JcrConstants.NT_FOLDER);
        }

        // Note that ResourceMetadata is NOT the data that populates a resources ValueMap; that is done below.
        ResourceMetadata resourceMetaData = new ResourceMetadata();

        // Set the resolution path
        resourceMetaData.setResolutionPath(path);

        // This resourceType is completely customizable
        // Often it is set in the OSGi Properties if the value is fixed for all resources this provider returns
        // It is important to ensure that any scripts associated w this resourceType stay in the Sling APIs and
        // do not drop down to the JCR Node APIs as this synthetic resource is a Sling abstraction and the JCR APIs
        // will see it as an invalid path/resource.
        final String resourceType = "samples/components/content/title";

        // Create the synthetic resource
        Resource resource = new SyntheticResource(resourceResolver, resourceMetaData, resourceType);

        // Make a call to some other system using the path/request and resolve the data to return on the "Provided" resource's ValueMap
        Map<String, Object> properties = new HashMap<String, Object>();

        // Mocking some data that represents this resource
        properties.put("sampleData", "This is sample data");
        properties.put("moreCustomData", "Some more data for the resource's value map");
        properties.put("jcr:created", new Date());
        properties.put("meaningOfLife", 42);

        // Add the properties for this resource by wrapping the synthetic resource with a ResourceWrapper (defined below)
        // that exposes a custom ValueMap via this resources .adaptTo(ValueMap.class)
        resource = new ProvidedResourceWrapper(resource, properties);

        return resource;
    }

    @Override
    public Iterator<Resource> listChildren(Resource parent) {
        final String path = parent.getPath();

        // If path is not the root, return null
        // This only allows listChildren to be called on a "Root" path
        // This restriction is implementation specific
        if (!isRoot(path)) {
            return null;
        }

        List<Resource> resources = new ArrayList<Resource>();

        // Call third party, get and create a list of resources in a similar fashion as in getResource
        for (int i = 0; i < 10; i++) {
            ResourceMetadata resourceMetaData = new ResourceMetadata();

            // Create the "path" for this resource; this pathing scheme must be compatible with getResource(..)
            resourceMetaData.setResolutionPath(path + "_" + i);
            resourceMetaData.put("index", String.valueOf(i));
            final String resourceType = "acs-samples/components/content/title";

            Resource resource = new SyntheticResource(parent.getResourceResolver(),
                    resourceMetaData, resourceType);

            // Create a ValueMap representation of this resource
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("sampleData", "This is sample data");
            resource = new ProvidedResourceWrapper(resource, properties);


            resources.add(resource);
        }

        return resources.iterator();
    }

    /**
     * Checks if the provided path is a defined Root path
     *
     * @param path
     * @return
     */
    protected boolean isRoot(String path) {
        for (String root : this.roots) {
            if (path.equals(root)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Custom Resource Wrapper that is used to expose a custom ValueMap via the "Provided" resource's .adaptTo(ValueMap.class);
     */
    private class ProvidedResourceWrapper extends ResourceWrapper {
        private final ValueMap properties;

        public ProvidedResourceWrapper(Resource resource, Map<String, Object> properties) {
            super(resource);
            this.properties = new ValueMapDecorator(properties);
        }

        @Override
        public final <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
            if (type != ValueMap.class) {
                return super.adaptTo(type);
            }

            // Return the ValueMap of the properties passed in
            return (AdapterType) this.properties;
        }
    }
}
