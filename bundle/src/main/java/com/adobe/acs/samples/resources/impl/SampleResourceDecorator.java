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

package com.adobe.acs.samples.resources.impl;

import com.adobe.acs.samples.resources.SampleResourceWrapper;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceDecorator;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


@Component(
        label = "ACS AEM Samples - Sample Resource Decorator",
        description = "Sample Sling Resource Decorator"
)
@Service
public class SampleResourceDecorator implements ResourceDecorator {
    private final Logger log = LoggerFactory.getLogger(SampleResourceDecorator.class);

    @Override
    public Resource decorate(final Resource resource) {
        // Remember to return early (as seen above) as this decorator is executed on all
        // Resource resolutions (this happens A LOT), especially if the decorator performs
        // any complex/slow running logic.

        if (!this.accepts(resource)) {
            return resource;
        }

        // Any overridden methods (ex. adaptTo) on the wrapper, will be used when invoked on the
        // resultant Resource object (even before casting).

        final ValueMap overlay = new ValueMapDecorator(new HashMap<String, Object>());
        overlay.put("foo", 100);
        overlay.put("cat", "meow");

        // See ACS AEM Samples com.adobe.acs.samples.resources.SampleResourceWrapper for how
        // these above overlay properties are added to the resource's ValueMap
        return new SampleResourceWrapper(resource, overlay);
    }

    @Deprecated
    @Override
    public Resource decorate(final Resource resource, final HttpServletRequest request) {
        // This is deprecated; Use decorate(Resource resource) instead.

        // Note; since ResourceDecorators are not called w Request context (this method being deprecated)
        // To pass in Request Context, use a ThreadLocal Sling Filter.
        // See ACS AEM Samples com.adobe.acs.samples.filters.impl.SampleThreadLocalFilter

        return this.decorate(resource);
    }

    // Check if this resource should be decorated. Return "false" as fast as possible.
    private boolean accepts(final Resource resource) {
        if (resource == null) {
            return false;
        }

        if (StringUtils.equals(resource.getPath(), "/some/path")) {
            return true;
        } else {
            return false;
        }

        // Note: If you are checking if a resource should be decorated based on resource type,
        // Using ResourceUtil.isA(..) will send this into an infinite recursive lookup loop
    }
}
