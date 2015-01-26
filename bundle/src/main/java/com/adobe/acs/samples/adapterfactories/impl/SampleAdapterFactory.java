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

package com.adobe.acs.samples.adapterfactories.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        label = "ACS AEM Samples - Adapter Factory",
        description = "",
        metatype = true
)
@Properties({
        @Property(
                label = "Adaptables",
                description = "Adaptables list the \"From\" objects the adapter supports",
                name = "adaptables",
                value = { "org.apache.sling.api.SlingHttpServletRequest" },
                propertyPrivate = true
        ),
        @Property(
                label = "Adapters",
                description = "Adapters list the \"To\" objects the adapter supports",
                name = "adapters",
                value = { "org.apache.sling.api.resource.Resource" },
                propertyPrivate = true
        )
})
@Service
public class SampleAdapterFactory implements AdapterFactory {
    private static final Logger log = LoggerFactory.getLogger(SampleAdapterFactory.class);

    @Override
    public final <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {

        // Ensure the adaptable object is of an appropriate type
        if (!(adaptable instanceof SlingHttpServletRequest) || (adaptable == null)) {
            log.warn("Always log when a object cannot be adapted.");
            return null;
        }

        Resource resource = ((SlingHttpServletRequest) adaptable).getResource();
        if (resource == null) {
            log.warn("Always log when a object cannot be adapted.");
        }

        return (AdapterType) resource;
    }
}