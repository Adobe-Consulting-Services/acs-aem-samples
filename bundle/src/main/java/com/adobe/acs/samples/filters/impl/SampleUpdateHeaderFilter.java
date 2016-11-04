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
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * updates the location header by stripping ".html" from the Location header for
 * the /content/geometrixx*
 * 
 * @author ravi
 */
@Component(policy = ConfigurationPolicy.OPTIONAL, metatype = true)
@Service
@Property(name = "pattern", value = "/content/geometrixx.*")
public class SampleUpdateHeaderFilter implements Filter {
    private static final String RESPONSE_HEADER_LOCATION = "Location";

    private class UpdateLocationResponse extends HttpServletResponseWrapper {

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        public UpdateLocationResponse(HttpServletRequest request,
                HttpServletResponse wrapped) {
            super(wrapped);
            this.request = request;
            this.response = wrapped;
        }

        @Override
        public void addHeader(String name, String value) {
            // this method will be called when adding any header, so we can
            // change the value of the added header once it's added by super
            // class
            super.addHeader(name, value);
            if (RESPONSE_HEADER_LOCATION.equals(name)) {
                updateLocationHeader();
            }
        }

        @Override
        public void flushBuffer() throws IOException {
            super.flushBuffer();
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            if (RESPONSE_HEADER_LOCATION.equals(name)) {
                updateLocationHeader();
            }
        }

        /**
         * updates location header if required
         */
        private void updateLocationHeader() {
            String locationHeader = response.getHeader("Location");
            if (locationHeader != null && locationHeader.endsWith(".html")) {
                locationHeader = locationHeader.substring(0,
                    locationHeader.lastIndexOf(".html"));
                response.setHeader("Location", locationHeader);
                log.info(
                    "updated the response header Location to [" + locationHeader
                        + "] for resource " + request.getPathInfo());
            }
        }
    }

    @Property(boolValue = false)
    private static final String PROP_ENABLED = "enabled";

    private boolean enabled;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    public void activate(ComponentContext ctx) {
        this.enabled = PropertiesUtil.toBoolean(
            ctx.getProperties().get(PROP_ENABLED), false);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (enabled && request instanceof HttpServletRequest
            && response instanceof HttpServletResponse) {
            UpdateLocationResponse cookieCaptureResponse = new UpdateLocationResponse(
                (HttpServletRequest) request, (HttpServletResponse) response);
            chain.doFilter(request, cookieCaptureResponse);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}