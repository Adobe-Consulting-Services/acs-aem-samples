package com.adobe.acs.samples.filters.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Felix" Filters are different from Sling Filters, in that they are executed by Apache Felix before the Sling Engine is engaged.
 *
 * This allows for:
 * - Processing a request before Sling Authentication and Resource Resolution has occurs.
 *
 * This prevents:
 * - Understanding accessing any Sling Context; Permissions, Resource Resolution, etc.
 *
 */

// Registered Felix Servlet Filters in AEM can be viewed her: http://localhost:4502/system/console/status-httpwhiteboard

@Component(
        service = Filter.class,
        property = {
                // A major difference from Sling Filters is Servlet Filters can be registered via the Felix HTTP Whiteboard to URL path patterns.

                // A Pattern OR Regex must be provided (Pick one or the other)

                // http://javadox.com/org.osgi/osgi.cmpn/6.0.0/org/osgi/service/http/whiteboard/HttpWhiteboardConstants.html#HTTP_WHITEBOARD_FILTER_PATTERN
                // Specify multiple values using the standard OSGi DS 1.2 Annotations convention of multiple property entries.
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/content/samples/",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/content/other-samples/",

                // http://javadox.com/org.osgi/osgi.cmpn/6.0.0/org/osgi/service/http/whiteboard/HttpWhiteboardConstants.html#HTTP_WHITEBOARD_FILTER_REGEX
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_REGEX + "=/[a-z]*",

                // http://javadox.com/org.osgi/osgi.cmpn/6.0.0/org/osgi/service/http/whiteboard/HttpWhiteboardConstants.html#HTTP_WHITEBOARD_CONTEXT_SELECT
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=*)"
        }
)
public class SampleServletFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(SampleServletFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Called once when the Filter is initially registered.
        // Usually, do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        // Since this context is that of a Felix HTTP Servlet Filter, we are guaranteed the request and response are HTTP Filters.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (accepts(httpServletRequest, httpServletResponse)) {

            // Do work before sending the request down the Felix Filter AND Sling processing chain...

            chain.doFilter(httpServletRequest, httpServletResponse);

            // The Request/Response have now been fully processed by Sling/AEM and coming out the other side of the Felix filter chain

        } else {
            // Else process the chain as usual.

            chain.doFilter(request, response);
        }
    }

    private boolean accepts(HttpServletRequest request, HttpServletResponse response) {
        // Perform FAST logic to determine if this filter should be invoked.
        // Return "false" fast and early, as this filter could effect huge numbers of requests.

        if (!"text/html".equals(request.getContentType())) {
            return false;
        }

        // Do other checks..

        return true;
    }

    @Override
    public void destroy() {
        // Called once when the Filter is unloaded.
        // Usually, do nothing
    }

    @Activate
    public void activate(ComponentContext ctx)  {
        // Since this is a @Component, the normal @Activate and @Deactivate methods are available
    }
}