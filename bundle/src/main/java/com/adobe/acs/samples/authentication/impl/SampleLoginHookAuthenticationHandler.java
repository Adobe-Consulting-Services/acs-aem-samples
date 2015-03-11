package com.adobe.acs.samples.authentication.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.auth.core.spi.AuthenticationFeedbackHandler;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Reference(target = "(service.pid=com.day.cq.auth.impl.LoginSelectorHandler)")
    private AuthenticationHandler wrappedAuthHandler;

    private boolean wrappedIsAuthFeedbackHandler = false;


    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return wrappedAuthHandler.extractCredentials(httpServletRequest, httpServletResponse);
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
        if (this.wrappedIsAuthFeedbackHandler) {
            ((AuthenticationFeedbackHandler) wrappedAuthHandler).authenticationFailed(httpServletRequest, httpServletResponse, authenticationInfo);
        }
    }

    @Override
    public boolean authenticationSucceeded(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationInfo authenticationInfo) {
        boolean result = false;
        
        if (this.wrappedIsAuthFeedbackHandler) {
            result = ((AuthenticationFeedbackHandler) wrappedAuthHandler).authenticationSucceeded(httpServletRequest, httpServletResponse, authenticationInfo);
        }

            log.error(">>>>> Hello from: {}", authenticationInfo.getUser());
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
}
