/*
 * #%L
 * ACS AEM Samples
 * %%
 * Copyright (C) 2017 Adobe
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

import com.adobe.acs.samples.SampleExecutor;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * This requires the following pom.xml configurations:
 *
 * Minimum of osgi.cmpn 6.0.0
 *
 *  <dependency>
 *      <groupId>org.osgi</groupId>
 *      <artifactId>osgi.cmpn</artifactId>
 *      <version>6.0.0</version>
 *  </dependency>
 *
 * Minimum of the 3.3.0 of the maven-bundle-plugin
 *
 * <plugin>
 *      <groupId>org.apache.felix</groupId>
 *      <artifactId>maven-bundle-plugin</artifactId>
 *      <version>3.3.0</version>
 * </plugin>
 *
 * Also see the great write up on new OSGi annotations by Nate Yolles: http://www.nateyolles.com/blog/2017/05/osgi-declarative-services-annotations-in-aem 
 */
@Component(
        // Provide the service property, and list of service interfaces if this @Component should be registered as a service
        service = { SampleExecutor.class },

        // Set the configurationPolicy
        configurationPolicy = ConfigurationPolicy.REQUIRE
)

// With @Designate, mark this OSGi service as taking the above Configuration class as the config to be passed into @Activate, @Deactivate and @Modified methods
@Designate(ocd = SampleOsgiR6AnnotationsImpl.Config.class)
public class SampleOsgiR6AnnotationsImpl implements SampleExecutor {
    private static final Logger log = LoggerFactory.getLogger(SampleOsgiR6AnnotationsImpl.class);

    // Define the OSGi Property Configuration DTO that will replace the OSGi Properties map passed into @Activate, @Deactivate and @Modified methods
    // This replaces the @Property
    @ObjectClassDefinition(
            name = "ACS AEM Samples - OSGi R6 Annotated Component",
            description = "This is generated via the OSGi R6 Annotated Component"
    )
    @interface Config {
        // The _'s in the method names (se below) are transformed to . when the OSGi property names are generated.
        // Example: max_size -> max.size, user_name_default -> user.name.default

        @AttributeDefinition(
                name = "Max size",
                description = "The maximum size",
                min = "10",
                max = "100",
                required = false, // Defaults to true
                cardinality = 0
        )
        int max_size() default 10;

        // Multiple values
        @AttributeDefinition(
                cardinality = 5
        )
        String[] items();

        // Options
        @AttributeDefinition(
            options = {
                    @Option(label = "Option Foo", value = "foo"),
                    @Option(label = "Option Bar", value = "bar"),
            }
        )
        String foo_bar() default "bar";
    }

    private Config config;


    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            service = SampleExecutor.class
    )
    private final Collection<SampleExecutor> sampleExecutors = new ArrayList<>();

    @Activate
    @Modified
    protected void activate(Config config) {
        log.info("Max Size from OSGi Configuration: {}", config.max_size());
        log.info("Items from OSGi Configuration: {}", Arrays.asList(config.items()));
        log.info("Selected Foo/Bar option from OSGi Configuration: {}", config.foo_bar());

        this.config = config;
    }

    @Deactivate
    protected void deactivate(Config config) {
        // Do deactivation work; this method also can take the Configuration object
    }

    @Override
    public String execute() {
        return null;
    }

    @Override
    public String execute(ResourceResolver resourceResolver) {
        return null;
    }
}
