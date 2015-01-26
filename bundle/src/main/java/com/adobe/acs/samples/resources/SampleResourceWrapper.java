package com.adobe.acs.samples.resources;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;
import java.util.Map;


public class SampleResourceWrapper extends ResourceWrapper {

    private ValueMap properties;

    // Creates a new wrapper instance delegating all method calls to the given resource,
    // and augments the Resource's properties valueMap with the values in the "overlayProperties" param
    public SampleResourceWrapper(final Resource resource, final ValueMap overlayProperties) {
        super(resource);

        final Map<String, Object> mergedProperties = new HashMap<String, Object>();
        mergedProperties.putAll(super.getValueMap());
        mergedProperties.putAll(overlayProperties);

        this.properties = new ValueMapDecorator(mergedProperties);
    }


    // When modifying the Resource's ValueMap, override .getValueMap()
    public final ValueMap getValueMap() {
        return this.properties;
    }


    // When modifying the Resource's ValueMap, override .adaptTo()
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type != ValueMap.class) {
            return super.adaptTo(type);
        }

        return (AdapterType) this.getValueMap();
    }
}
