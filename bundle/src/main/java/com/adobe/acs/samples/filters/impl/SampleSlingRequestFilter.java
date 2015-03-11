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

import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@SlingFilter(
        label = "ACS AEM Samples - Sling REQUEST Filter",
        description = "Sample implementation of a Sling Filter",
        metatype = true,
        generateComponent = true, // True if you want to leverage activate/deactivate or manage its OSGi life-cycle
        generateService = true, // True; required for Sling Filters
        order = 0, // The smaller the number, the earlier in the Filter chain (can go negative);
                    // Defaults to Integer.MAX_VALUE which push it at the end of the chain
        scope = SlingFilterScope.REQUEST) // REQUEST, INCLUDE, FORWARD, ERROR, COMPONENT (REQUEST, INCLUDE, COMPONENT)
public class SampleSlingRequestFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SampleSlingRequestFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Usually, do nothing
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // Since this is a Sling Filter, the request and response objects are guaranteed to be of types
        // SlingHttpServletRequest and SlingHttpServletResponse.

        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        final Resource resource = slingRequest.getResource();

        if (resource.getPath().startsWith("/redirect-me")) {

            // Is the SlingFilterScope is REQUEST, redirects can be issued.
            slingResponse.sendRedirect("/some/redirect.html");

            // Stop processing the request chain
            return;
        }

        // Content can be written tot he response before and after the chain execution
        
        // Forcing false in this sample so else this will break AEM when installed
        if (false && response.getContentType().contains("html")) {
            // In this example, checking for html response type since the comments are HTML format and would break
            // binary, json, etc. responses

            // Write some more content to the response before this chain has executed
            //response.getWriter().write("<!-- Written from the Sample Sling Filter BEFORE the next include -->");

            // Proceed with the rest of the Filter chain
            chain.doFilter(request, response);

            // Write some more content to the response after this chain has executed
            //response.getWriter().write("<!-- Written from the Sample Sling Filter AFTER the next include -->");
        }  else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Usually, do nothing
    }
}