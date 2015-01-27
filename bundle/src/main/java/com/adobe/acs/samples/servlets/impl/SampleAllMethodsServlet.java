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

package com.adobe.acs.samples.servlets.impl;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

/**
 * @author david
 */
@SlingServlet(
        label = "Samples - Sling All Methods Servlet",
        description = "Sample implementation of a Sling All Methods Servlet.",
        paths = { "/services/all-sample" },
        methods = { "GET", "POST" }, // Ignored if paths is set - Defaults to GET if not specified
        resourceTypes = { }, // Ignored if paths is set
        selectors = { "print.a4" }, // Ignored if paths is set
        extensions = { "html", "htm" }  // Ignored if paths is set
)
public class SampleAllMethodsServlet extends SlingAllMethodsServlet implements OptingServlet {
    private static final Logger log = LoggerFactory.getLogger(SampleAllMethodsServlet.class);

    /**
     * Add overrides for other SlingAllMethodsServlet here (doHead, doTrace, doPut, doDelete, etc.)
     */

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws
            ServletException, IOException {
        // Implement custom handling of GET requests
        // This should be idempotent and not change underlying data in any meaningful way;
        // To be safe, never modify data (add/update/delete) in the context of a GET request

        // Set the response type; this might be JSON, etc.
        // The repsonse type is usually closely correlated to the extension the servlet listens on
        response.setContentType("text/html");

        // Do some work
        Resource resource = request.getResourceResolver().getResource("/content/world");
        ValueMap properties = resource.adaptTo(ValueMap.class);

        if (properties != null) {
            // Writing HTML in servlets is usually inadvisable, and is better suited to be provided via a JSP/Sightly template
            // This is just an example.
            response.getWriter().write("<html><head></head><body>Hello "
                            + properties.get("name", "World")
                            + "!</body></html>");
            // By Default the 200 HTTP Response status code is used; below explicitly sets it.                    
            response.setStatus(SlingHttpServletResponse.SC_OK);
        } else {
            // Set HTTP Response Status code appropriately
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("ERROR");
        }
    }

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws
            ServletException, IOException {
        // Implement custom handling of POST requests

        // Get Request parameter value
        String newWorld = request.getParameter("world");

        // Do some work
        Resource resource = request.getResourceResolver().getResource("/content/world");
        ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
        properties.put("name", newWorld);
        resource.getResourceResolver().commit();

        // Set the content type as needed
        // The repsonse type is usually closely correlated to the extension the servlet listens on.
        response.setContentType("application/json");
        
        // When constructing a JSON response, leverage the Sling JSON Apis
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse.put("success", true);
            jsonResponse.put("new-world", newWorld);
            // Write the JSON to the response
            response.getWriter().write(jsonResponse.toString(2));
            // Be default, a 200 HTTP Response Status code is used
        } catch (JSONException e) {
            log.error("Could not formulate JSON response");
            // Servlet failures should always return an approriate HTTP Status code
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // If you do not set your own HTML Response content, the OOTB HATEOS Response is used
            response.getWriter().write("ERROR");
        }
    }

    /** OptingServlet Acceptance Method **/

    @Override
    public final boolean accepts(SlingHttpServletRequest request) {
        /*
         * Add logic which inspects the request which determines if this servlet
         * should handle the request. This will only be executed if the
         * Service Configuration's paths/resourcesTypes/selectors accept the request.
         */
        return true;
    }

}
