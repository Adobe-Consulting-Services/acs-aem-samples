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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptables list the "From" objects the adapter supports
 * Adapters list the "To" objects the adapter supports
 */
@Component(
        service = AdapterFactory.class,
        property = {
                "adaptables=org.apache.sling.api.SlingHttpServletRequest",
                "adapters=org.apache.sling.api.resource.Resource",
        }
)
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