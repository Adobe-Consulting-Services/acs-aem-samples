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

package com.adobe.acs.samples.resources;

import org.apache.sling.adapter.annotations.Adaptable;
import org.apache.sling.adapter.annotations.Adapter;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This Sample is heavily inspired by the Sling PlanetResource example:
 * >  http://svn.apache.org/repos/asf/sling/trunk/launchpad/test-services/src/main/java/org/apache/sling/launchpad/testservices/resourceprovider/PlanetResource.java
 */

// Mark this resource as Adaptable to a ValueMap; Other adapters can be defined here as well.
@Adaptable(adaptableClass = Resource.class,
        adapters = {
                @Adapter({ ValueMap.class })
        }
)
public class SampleResource extends AbstractResource implements Resource {

    // The resource's path; This does not have to map to a "real" JCR path
    private final String path;

    // ResourceMetadata; Note this is NOT the resource's ValueMap data
    private final ResourceMetadata metadata;

    // The ValueMap that holds the resource's "property" data representation
    private final ValueMap valueMap;

    // The ResourceResolver associated with this Resource
    private final ResourceResolver resourceResolver;

    // The Resource's resourceType; this can map to any resourceType
    public static final String RESOURCE_TYPE = "samples/components/foo";

    // Define the ValueMap that will be used to populate the ValueMap param in this resource's Constructor (3rd param)
    public static class SampleValueMap extends ValueMapDecorator {

        // Common patterns include enumerating all the properties and adding each specifically
        // This allows for a controlled property schema
        public SampleValueMap(final String name, final int height, final int width) {
            super(new HashMap<String, Object>());

            put("name", name);
            put("height", height);
            put("width", width);
        }

        // Common patterns include allowing any set of Map of data to be set
        // This allows for a flexible property schema
        public SampleValueMap(final Map<String, Object> properties) {
            // Call the super cstor with an empty map
            super(new HashMap<String, Object>());

            // Put all the properties into the map
            putAll(properties);
        }
    }

    public SampleResource(final ResourceResolver resourceResolver, final String path, final ValueMap valueMap) {
        this.path = path;
        this.resourceResolver = resourceResolver;

        // Set the properties for the ValueMap
        this.valueMap = valueMap;

        // Other "universal" properties can be added here
        this.valueMap.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, RESOURCE_TYPE);
        this.valueMap.put("generatedAt", new Date());

        // Set the resolutionPath for this resource to the resource's path
        this.metadata = new ResourceMetadata();
        this.metadata.setResolutionPath(path);

        new SampleResource.SampleValueMap("foo", 2, 3);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + this.getPath();
    }

    public String getPath() {
        return this.path;
    }

    public ResourceMetadata getResourceMetadata() {
        return this.metadata;
    }

    public ResourceResolver getResourceResolver() {
        return this.resourceResolver;
    }

    public String getResourceSuperType() {
        // Optionally set the resource's superType
        return null;
    }

    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    // Implement getValueMap() to support getting valueMap without using "adaptTo(..)"
    public ValueMap getValueMap() {
        return this.valueMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == ValueMap.class) {
            return (AdapterType) valueMap;
        }

        return super.adaptTo(type);
    }
}