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

package com.adobe.acs.samples.filters.impl;


import com.adobe.acs.samples.filters.SampleThreadLocalService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/*
    The ThreadLocal variable defined in this Http Servlet Filter can now be accessed anywhere in the context of this
    thread. An example use case is Decorating a Sling Resource (via ResourceDecorators) based on some facet of the
    associated HTTP Request (since the HTTP Request is not available to ResourceDecorators).

    If variables need ot be passed along threads IN the context of the HTTP Request, it is better to set them via
    request.setAttribute(..) and retrieve them via request.getAttribute(..).

    In the case of this ThreadLocal variable it can be retrieved form an OSGi Service as follows:

    public ConsumerServiceImpl implements ConsumerService {

        @Reference
        SampleThreadLocalService sampleThreadLocalService;

        public final String getMessage() {
            if (sampleThreadLocalService.get()) {
                return "Welcome to preview mode!"
            } else {
                return "Welcome to normal mode!";
            }
        }

    }
*/

/**
 *  This is a OGSi service that is a Sling Filter which sets ThreadLocal state AND an OSGi Service
 *  that exposes the ThreadLocal state to other OSGi Services."
 */

@Component(
        // See documentation for the @SlingServletFilter configuration here: https://sling.apache.org/documentation/the-sling-engine/filters.html
        property = {
                "sling.filter.scope=request",
                "filter.order=" + Integer.MIN_VALUE
        }
        // Note tht this could be achieved via a combination OSGi properties (for sling.filter.order) and @SlingServletFilter convenience setters
)
public class SampleThreadLocalFilter implements Filter, SampleThreadLocalService {
    private static final Logger log = LoggerFactory.getLogger(SampleThreadLocalFilter.class);

    // Define the ThreadLocal object; the Generic type (in this case Boolean) defines what
    // data type the ThreadLocal object will store
    // Since this is ThreadLocal w dont need to wory about concurrency or other Threads modifying this value.
    private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() {
            set(false);
            return get();
        }
    };

    @Override
    public void init(final FilterConfig filterConfig) {
        // Usually, do nothing
    }

    @Override
    public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        // Since we are in the context of a SlingFilter; the Request is always a SlingHttpServletRequest
        SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;

        // Set the thread local value based on your use case; in this case the thread local variable is set to "true"
        // if a HTTP Request parameter of "preview" is provided
        if (slingHttpServletRequest.getParameter("preview") != null) {
            THREAD_LOCAL.set(true);
        }

        try {
            // Continue processing the request chain
            chain.doFilter(request, response);
        } finally {
            // Good housekeeping; Clean up after yourself!!!
            THREAD_LOCAL.remove();
        }
    }


    /**
     * This is required by the implementation of the SampleThreadLocalService interface.
     * This is the method that will expose the ThreadLocal variable to other OSGi Services.
     *
     * @return the thread local value
     */
    @Override
    public final boolean get() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void destroy() {
        // Usually, do nothing
    }
}
