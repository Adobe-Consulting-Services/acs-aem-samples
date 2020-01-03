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
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = SampleService.class,
        property = {
                SampleService.PROP_NAME + "=" + "my-sample"
        }
)
@Designate(ocd = SampleServiceImpl.Cfg.class)
public class SampleServiceImpl implements SampleService {
    private static final Logger log = LoggerFactory.getLogger(SampleServiceImpl.class);

    private Cfg cfg;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /* Service Methods */

    @Override
    public final String helloWorld() {
        return String.format("Hello %s!", cfg.planet());
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
    protected final void activate(final Cfg cfg) throws Exception {
        // Read in OSGi Properties for use by the OSGi Service in the Activate method

        // Ideally the Cfg's getters provide values as is, but often this is a good place to parse more complex values
        // of the OSGi service, such as Strings to Patterns, or splitting up key/value pairs passed in as Strings,
    }

    @Deactivate
    protected final void deactivate(final Cfg cfg) {
        // Usually don't have to do much here.
    }

    @ObjectClassDefinition(name = "ACS AEM Samples - Basic OSGi Service Configuration")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Planet",
                description = "The planet from which to greet from."
        )
        String planet() default "Earth";
    }
}