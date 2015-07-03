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

package com.adobe.acs.samples.services.impl;

import com.adobe.acs.samples.services.SampleService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(
        label = "ACS AEM Samples - Basic OSGi Service",
        description = "Sample implementation of an OSGi service",
        metatype = true,
        configurationFactory = true)
@Properties({
    @Property(
        label = "Service Name",
        name = SampleService.PROP_NAME,
        description = "This is an example property which is used to uniquely identify the service impl by name.",
        value = "my-sample"
    )
})
@Service
public class SampleServiceImpl implements SampleService {
    private static final Logger log = LoggerFactory.getLogger(SampleServiceImpl.class);

    /* OSGi Properties */
    private static final String DEFAULT_WORLD = "earth";
    private String world = DEFAULT_WORLD;
    @Property(label = "World",
            description = "The world",
            value = DEFAULT_WORLD)
    public static final String PROP_WORLD = "world";

    /* OSGi Service References */

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /* Service Methods */

    @Override
    public final String helloWorld() {
        return String.format("Hello %s!", this.world);
    }

    @Override
    public final void doWork(final ResourceResolver resourceResolver) throws PersistenceException {
        // Generally you always want to pass in the security context (JCR Session/Resource Resolver, Resource, etc.)
        // into OSGi services rather than using administrative level repo access.

        // Do some work...
        Resource resource = resourceResolver.getResource("/content/some/resource");
        ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
        properties.put("workDone", true);
        resourceResolver.commit();
    }

    /* OSGi Component Methods */

    @Activate
    protected final void activate(final Map<String, String> properties) throws Exception {
        // Read in OSGi Properties for use by the OSGi Service in the Activate method
        this.world = PropertiesUtil.toString(properties.get(PROP_WORLD), DEFAULT_WORLD);
    }

    @Deactivate
    protected final void deactivate(final Map<String, String> properties) {
        // Remove method is not used
    }
}