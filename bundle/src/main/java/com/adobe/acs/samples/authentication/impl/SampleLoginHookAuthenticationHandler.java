package com.adobe.acs.samples.authentication.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.auth.core.spi.AuthenticationFeedbackHandler;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Map;


@Component(
        label = "ACS AEM Samples - Authentication Login Hook"
)
@Properties({
        @Property(name = "service.ranking", intValue = 10000),
        @Property(name = "path", value = "/content")
})
@Service
public class SampleLoginHookAuthenticationHandler implements AuthenticationHandler, AuthenticationFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(SampleLoginHookAuthenticationHandler.class);

    @Reference(target = "(service.pid=com.day.crx.security.token.impl.impl.TokenAuthenticationHandler)")
    private AuthenticationHandler wrappedAuthHandler;

    private boolean wrappedIsAuthFeedbackHandler = false;

    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // Wrap the response object to capture any calls to sendRedirect(..) so it can be released in a controlled
        // manner later.
        final DeferredRedirectHttpServletResponse deferredRedirectResponse =
                new DeferredRedirectHttpServletResponse(httpServletRequest, httpServletResponse);

        return wrappedAuthHandler.extractCredentials(httpServletRequest, deferredRedirectResponse);
    }

    @Override
    public boolean requestCredentials(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        return wrappedAuthHandler.requestCredentials(httpServletRequest, httpServletResponse);
    }

    @Override
    public void dropCredentials(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        wrappedAuthHandler.dropCredentials(httpServletRequest, httpServletResponse);
    }

    @Override
    public void authenticationFailed(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationInfo authenticationInfo) {
        // Wrap the response so we can release any previously deferred redirects
        final DeferredRedirectHttpServletResponse deferredRedirectResponse =
                new DeferredRedirectHttpServletResponse(httpServletRequest, httpServletResponse);

        if (this.wrappedIsAuthFeedbackHandler) {
            ((AuthenticationFeedbackHandler) wrappedAuthHandler).authenticationFailed(httpServletRequest, deferredRedirectResponse, authenticationInfo);
        }

	try {
            deferredRedirectResponse.releaseRedirect();
        } catch (IOException e) {
            log.error("Could not release redirect", e);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean authenticationSucceeded(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationInfo authenticationInfo) {
        boolean result = false;

        // Wrap the response so we can release any previously deferred redirects
        final DeferredRedirectHttpServletResponse deferredRedirectResponse =
                new DeferredRedirectHttpServletResponse(httpServletRequest, httpServletResponse);

        if (this.wrappedIsAuthFeedbackHandler) {
            result = ((AuthenticationFeedbackHandler) wrappedAuthHandler).authenticationSucceeded(httpServletRequest, httpServletResponse, authenticationInfo);
        }

	try {
            deferredRedirectResponse.releaseRedirect();
        } catch (IOException e) {
            log.error("Could not release redirect", e);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return result;
    }


    @Activate
    protected void activate(final Map<String, String> config) {
        this.wrappedIsAuthFeedbackHandler = false;

        if (wrappedAuthHandler != null) {
            log.debug("Registered wrapped authentication feedback handler");
            this.wrappedIsAuthFeedbackHandler = wrappedAuthHandler instanceof AuthenticationFeedbackHandler;
        }
    }


    /**
     * It is not uncommon (Example: OOTB SAML Authentication Handler) for response.sendRedirect(..) to be called
     * in extractCredentials(..). When sendRedirect(..) is called, the response immediately flushes causing the browser
     * to redirect.
     */
    private class DeferredRedirectHttpServletResponse extends HttpServletResponseWrapper {
        private String ATTR_KEY = DeferredRedirectHttpServletResponse.class.getName() + "_redirectLocation";

        private HttpServletRequest request = null;

        public DeferredRedirectHttpServletResponse(final HttpServletRequest request, final HttpServletResponse response) {
            super(response);
            this.request = request;
        }


        /**
         * This method captures the redirect request and stores it to the Request so it can be leveraged later.
         * @param location the location to redirect to
         */
        @Override
        public void sendRedirect(String location) {
            // Capture the sendRedirect location, and hold onto it so it can be released later (via releaseRedirect())
            this.request.setAttribute(ATTR_KEY, location);
        }

        /**
         * Invokes super.sendRedirect(..) with the value captured in this.sendRedirect(..)
         * @throws IOException 
         */
        public final void releaseRedirect() throws IOException {
            final String location = (String) this.request.getAttribute(ATTR_KEY);

            if (location != null) {
                super.sendRedirect(location);
            }
        }
    }
}
