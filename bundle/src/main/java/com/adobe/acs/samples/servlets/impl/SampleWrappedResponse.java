package com.adobe.acs.samples.servlets.impl;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapped Response and Requests are often injected into the request processing at:
 * - The Filter level, if this change is far-reaching
 * - The Servlet level, if this change is servlet-specific
 */
public final class SampleWrappedResponse extends HttpServletResponseWrapper {
    private final Logger log = LoggerFactory.getLogger(SampleWrappedResponse.class);

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public SampleWrappedResponse(HttpServletRequest request, HttpServletResponse wrapped) {
        // Call super on the Wrapped response
        super(wrapped);

        // Extra state can be passed in and persisted if methods within the wrapped obj need to reference them.
        this.request = request;

        // Optionally the original state can be persisted; However the Response should always be available via super.XXX.
        this.response = wrapped;
    }

    /**
     * REMEMBER, the Request/Response Wrapper APIs may have multiple methods to effect similar change.
     * In this case, we must trap changed via addHeader(..) and setHeader(..) as they both modify the Response Headers.
     *
     * This method will be called whenever .addHeader(..) is called by other code on the Response object.
     * We can intercept and change the values coming in based on our custom logic.
     *
     * @param name the header name
     * @param value the header value
     */
    @Override
    public void addHeader(String name, String value) {
        // In this example we will remove .html from any Value that is set to the Location header.
        if ("Location".equals(name)) {
            // We can call super.addHeader(..) and pass in our customized value, that has .html removed from the end.
            String updatedLocation = getUpdatedLocation(value);
            super.addHeader(name, updatedLocation);
            log.info("addHeader(..) 'Location' to [ {} ] for Request [ {} ]", updatedLocation, request.getPathInfo());

        } else {
            // If the addHeader(..) call is NOT for 'Location', we simply pass to the super.addHeader(..) and let it be handled normally.
            super.addHeader(name, value);
        }
    }

    /**
     * REMEMBER, the Request/Response Wrapper APIs may have multiple methods to effect similar change.
     * In this case, we must trap changed via addHeader(..) and setHeader(..) as they both modify the Response Headers.
     *
     * This method will be called whenever .setHeader(..) is called by other code on the Response object.
     * We can intercept and change the values coming in based on our custom logic.
     *
     * @param name the header name
     * @param value the header value
     */
    @Override
    public void setHeader(String name, String value) {
        if ("Location".equals(name)) {
            // We can call super.setHeader(..) and pass in our customized value, that has .html removed from the end.
            String updatedLocation = getUpdatedLocation(value);
            super.setHeader(name, updatedLocation);
            log.info("setHeader(..) 'Location' to [ {} ] for Request [ {} ]", updatedLocation, request.getPathInfo());

        } else {
            // If the setHeader(..) call is NOT for 'Location', we simply pass to the super.setHeader(..) and let it be handled normally.
            super.setHeader(name, value);
        }
    }

    /**
     * This method perform the sample work of removing ".html" from the Location header value.
     * @param value the original location value
     * @return the updated location value
     */
    private String getUpdatedLocation(String value) {
        return StringUtils.removeEndIgnoreCase(value, ".html");
    }
}