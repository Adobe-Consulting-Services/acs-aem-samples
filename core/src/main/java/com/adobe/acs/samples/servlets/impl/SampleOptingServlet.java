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
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;

import static org.apache.sling.api.servlets.ServletResolverConstants.*;

// So full list of options here: https://sling.apache.org/documentation/the-sling-engine/servlets.html#registering-a-servlet-using-java-annotations

// It is almost always better to register a Sling Servlet to a RESOURCE TYPE and NOT a PATH.
// If you are registering to a PATH there is likely something broken in your application design and you should revisit the decision.


/**
 COMMON PATTERN OF OPTING SERVLET IN AEM

  A common pattern of the opting servlet is to attach custom Selectors or Extensions to a cq:Page or dam:Asset node. The example below shows this for cq:Page.

  - HTTP GET /content/pages/page.sample.json

  The reason the opting servlet is useful, is because the /content/pages/page resource's resource type is actually cq/Page (which is the jcr:primaryType).
  You can bind to the this resource type (cq/Page) but you may want to only bind to it for your custom Page implementations (vs EVERY cq:Page in the system).
  To do this, you can use the OptingServet's accepts method to check if the cq:Page's jcr:content node's sling:resourceType is of your Page type, if it is
  then process accordingly, otherwise let the default cq/Page servlet handle the request.
 **/
@Component(
        service = { Servlet.class },
        property = {
                SLING_SERVLET_RESOURCE_TYPES + "=cq/Page",
                SLING_SERVLET_METHODS + "=GET",
                SLING_SERVLET_SELECTORS + "=sample",
                SLING_SERVLET_EXTENSIONS + "=json"
        }
)
public class SampleOptingServlet extends SlingSafeMethodsServlet implements OptingServlet {
    private static final Logger log = LoggerFactory.getLogger(SampleOptingServlet.class);

    /**
     * Accepts lets custom logic control the determination if this servlet should process the request.
     *
     * @param slingHttpServletRequest
     * @return true for the servlet to process this request, false to let another servlet try.
     */
    @Override
    public boolean accepts(final SlingHttpServletRequest slingHttpServletRequest) {
        if (slingHttpServletRequest.getResource().getChild("jcr:content").isResourceType("acs-samples/components/structure/page")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add overrides for other SlingSafeMethodsServlet here (doGeneric, doHead, doOptions, doTrace) *
     */
    @Override
    protected final void doGet(final SlingHttpServletRequest request,final SlingHttpServletResponse response) {
        // Do work to create a own "sample" JSON rendition. Since this will only be invoked with accepts(..) returns true,
        // You can assume request.getResource() => [cq:Page]/jcr:content@sling:resourceType = acs-samples/components/structure/page
        // since that's what accepts(..) checks for.
        // You can of course do any sort of more complex checks in accepts you need to.
    }
}