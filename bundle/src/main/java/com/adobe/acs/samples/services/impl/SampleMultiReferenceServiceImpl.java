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

import com.adobe.acs.samples.services.SampleMultiReferenceService;
import com.adobe.acs.samples.services.SampleService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(
        label = "ACS AEM Samples - Multi-reference Service",
        description = "This sample service collects registered implementations of other services."
)
@References({
        @Reference(
                name = SampleMultiReferenceServiceImpl.SAMPLE_SERVICE_NAME,
                referenceInterface = SampleService.class,
                policy = ReferencePolicy.DYNAMIC,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
})
@Service
public class SampleMultiReferenceServiceImpl implements SampleMultiReferenceService {
    private final Logger log = LoggerFactory.getLogger(SampleMultiReferenceServiceImpl.class);

    public static final String SAMPLE_SERVICE_NAME = "sampleService";

    // List to store Service objects derived from the serviceReferenceArray
    // Note: This Map MUST be thread-safe; there is no guarentee that OSGi will not be adding/removing 
    // service references from this Map while consuming code is reading from it.
    private Map<String, SampleService> sampleServices = new ConcurrentHashMap<String, SampleService>();

    /* Service Methods */

    @Override
    public final List<String> helloWorlds() {
        final List<String> results = new ArrayList<String>();

        for (final Map.Entry<String, SampleService> entry : sampleServices.entrySet()) {
            results.add(entry.getValue().helloWorld());
        }

        return results;
    }

    // These methods are named based on the @Reference(name = ...) value
    // bind<name>(..)
    protected final void bindSampleService(final SampleService service,
                                                    final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(SampleService.PROP_NAME), null);
        if (type != null) {
            this.sampleServices.put(type, service);
        }
    }

    // These methods are named based on the @Reference(name = ...) value
    // unbind<name>(..)
    protected final void unbindSampleService(final SampleService service,
                                                      final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(SampleService.PROP_NAME), null);
        if (type != null) {
            this.sampleServices.remove(type);
        }
    }
}
