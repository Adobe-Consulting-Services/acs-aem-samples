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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.sling.api.servlets.ServletResolverConstants.*;

// So full list of options here: https://sling.apache.org/documentation/the-sling-engine/servlets.html#registering-a-servlet-using-java-annotations

// It is almost always better to register a Sling Servlet to a RESOURCE TYPE and NOT a PATH.
// If you are registering to a PATH there is likely something broken in your application design and you should revisit the decision.
@Component(
        service = { Servlet.class },
        property = {
                SLING_SERVLET_RESOURCE_TYPES + "=/acs-sample/my/resource/type",
                SLING_SERVLET_METHODS + "=GET",
                SLING_SERVLET_EXTENSIONS + "=html",
                SLING_SERVLET_SELECTORS + "=hello",
                SLING_SERVLET_SELECTORS + "=howdy",
        }
        // Registering multiple values simply requires multiple key/value pairs for the same key.
)
public class SampleSafeMethodsServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(SampleSafeMethodsServlet.class);
    private static final int FOUR_KB = 4096;

    /**
     * Add overrides for other SlingSafeMethodsServlet here (doGeneric, doHead, doOptions, doTrace) *
     */

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        // Implement custom handling of GET requests
        boolean responseIsText = true;

        if (responseIsText) {

            // Write a standard text/html response
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("<html><body>Text to write to response</body></html>");

        } else {
            // Write some binary data to the response; Such as sending back an Image or PDF
            InputStream input = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };

            response.setContentType(getServletContext().getMimeType(request.getPathInfo()));
            OutputStream output = response.getOutputStream();

            byte[] buffer = new byte[FOUR_KB];

            for (int length = 0; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
                output.flush();
            }
        }
    }
}