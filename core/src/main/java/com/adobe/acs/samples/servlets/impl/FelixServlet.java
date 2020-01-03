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


import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
    service = Servlet.class,
    property = {
            // http://javadox.com/org.osgi/osgi.cmpn/6.0.0/org/osgi/service/http/whiteboard/HttpWhiteboardConstants.html#HTTP_WHITEBOARD_SERVLET_PATTERN
            HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/bin/sample/felix/servlet",
            HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=org.osgi.service.http)"
    }
)
public class FelixServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle HTTP GET requests
        response.getWriter().write("HTTP GET: Ok");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle HTTP POST requests
        response.getWriter().write("HTTP POST: Ok");
    }
}
