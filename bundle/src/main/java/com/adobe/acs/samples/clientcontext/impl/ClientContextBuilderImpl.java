package com.adobe.acs.samples.clientcontext.impl;


import com.adobe.acs.samples.clientcontext.ClientContextBuilder;
import com.adobe.acs.samples.clientcontext.ClientContextStore;
import com.adobe.granite.xss.ProtectionContext;
import com.adobe.granite.xss.XSSFilter;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.WCMMode;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(
        label = "ACS-AEM-Samples - Client Context Builder",
        description = "Service to build out custom Client Contexts",
        metatype = false,
        immediate = true,
        policy = ConfigurationPolicy.REQUIRE
)
@Properties({
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
@Service
public class ClientContextBuilderImpl implements ClientContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(ClientContextBuilderImpl.class);

    @Reference
    private XSSFilter xss;

    @Reference
    private Externalizer externalizer;

    @Reference
    private SlingRepository slingRepository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public JSONObject getJSON(SlingHttpServletRequest request,
                              ClientContextStore store) throws JSONException {
        if (store.handleAnonymous() && isAnonymous(request)) {
            log.debug("Get Anonymous JSON");
            return store.getAnonymousJSON(request);
        } else {
            log.debug("Get User JSON");
            return store.getJSON(request);
        }
    }
    @Override
    public JSONObject xssProtect(JSONObject json, String... whitelist) throws JSONException {
        final List<String> keys = IteratorUtils.toList(json.keys());
        final boolean useWhiteList = !ArrayUtils.isEmpty(whitelist);
        log.debug("Use Whitelist: " + !ArrayUtils.isEmpty(whitelist));

        for (final String key : keys) {
            log.debug("XSS Key: " + key);
            if (!useWhiteList || (useWhiteList && !ArrayUtils.contains(whitelist, key))) {
                log.debug("XSS -> " + key + XSS_SUFFIX + ": " + xss.filter(ProtectionContext.PLAIN_HTML_CONTENT, json.optString(key)));
                json.put(key + XSS_SUFFIX, xss.filter(ProtectionContext.PLAIN_HTML_CONTENT, json.optString(key)));
            }
        }

        log.debug("XSS JSON: " + json.toString(4));

        return json;
    }
    @Override
    public String getInitJavaScript(JSONObject json, ClientContextStore store) {
        return getInitJavaScript(json, store.getContextStoreManagerName());
    }

    @Override
    public String getInitJavaScript(JSONObject json, String manager) {
        String script = "";
        Iterator<String> keys = json.keys();

        while (keys.hasNext()) {
            final String key = keys.next();
            script += getAddInitProperty(manager, key, json.optString(key));
        }

        return wrapWithAnonymousScope(script);
    }
    @Override
    public boolean isSystemProperty(String key) {
        return (!key.startsWith("jcr:") && !key.startsWith("sling:") && !key.startsWith("cq:last"));
    }

    @Override
    public String getGenericInitJS(SlingHttpServletRequest request, ClientContextStore store) throws JSONException {
        final String manager = store.getContextStoreManagerName();
        final String json = getJSON(request, store).toString();

        final String script =
                wrapWithManagerCheck("CQ_Analytics." + manager + ".loadInitProperties(" + json + ", true);", manager);

        return wrapWithAnonymousScope(script);
    }

    @Override
    public String getAuthorizableId(SlingHttpServletRequest request) {
        if (StringUtils.isBlank(request.getQueryString())) {
            // If the request does not have Query Params no matter what.
            // We do not want to have a chance of caching non-anonymous personalized content.
            log.debug("QP is blank; Is Anonymous");

            return ANONYMOUS;
        }

        final AuthorizableResolution authorizableResolution = getAuthorizableResolution(request);
        final WCMMode wcmMode = WCMMode.fromRequest(request);

        if (wcmMode == null || WCMMode.DISABLED.equals(wcmMode)) {
            // Publish mode
            log.debug("Publish WCM Mode");

            // Always look at the user Sling has associated with the Request in Publish mode
            final ResourceResolver resourceResolver = request.getResourceResolver();
            // TODO: better way to get AuthorizableId?
            final String userId = resourceResolver.getUserID();

            if (resourceResolver == null || StringUtils.equals(ANONYMOUS, userId)) {
                log.debug("Is Anonymous");
                return ANONYMOUS;
            } else {
                log.debug("Is User: " + userId);
                return userId;
            }
        } else {
            // Author mode; Allow impersonations by author using the Clickstream Cloud
            log.debug("Author WCM Mode");

            if (AuthorizableResolution.IMPERSONATION.equals(authorizableResolution)) {
                // Get the authorizableId from the Query Params
                log.debug("Get authorizableId from QP");
                return StringUtils.strip(getParameterOrAttribute(request, AUTHORIZABLE_ID, null));
            } else if (AuthorizableResolution.AUTHENTICATION.equals(authorizableResolution)) {
                // Check the user Sling has associated with the request
                final ResourceResolver resourceResolver = request.getResourceResolver();
                final String userId = resourceResolver.getUserID();

                if (resourceResolver == null || StringUtils.equals(ANONYMOUS, userId)) {
                    log.debug("Is Anonymous");
                    return ANONYMOUS;
                } else {
                    log.debug("Is User: " + userId);
                    return userId;
                }
            }
        }

        // Should never happen, but when in doubt, treat as anonymous
        log.debug("Failed through to Anonymous");
        return ANONYMOUS;
    }

    @Override
    public String getPath(SlingHttpServletRequest request) {
        final String path = StringUtils.stripToNull(getParameterOrAttribute(request, PATH, null));
        if (path == null) {
            return path;
        }

        final ResourceResolver resourceResolver = request.getResourceResolver();
        return resourceResolver.map(request, path);
    }

    @Override
    public ResourceResolver getResourceResolverFor(final String authorizableId) throws LoginException {
        final Map<String, Object> serviceParams = new HashMap<String,Object>();
        //assuming the authorizable id is a service user. May be we can hard code a service user.
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, authorizableId);
        return resourceResolverFactory.getServiceResourceResolver(serviceParams);

    }

    @Override
    public void closeResourceResolverFor(ResourceResolver resourceResolver) {
        try {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        } finally {
            resourceResolver = null;
        }
    }

    private boolean isAnonymous(SlingHttpServletRequest request) {
        return StringUtils.equals(ANONYMOUS, getAuthorizableId(request));
    }
    private String getAddInitProperty(String manager, String key, String value) {
        if (StringUtils.isBlank(manager)) {
            throw new IllegalArgumentException("Client Context Data Manager cannot be blank.");
        } else if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Key cannot be blank.");
        }

        return ";CQ_Analytics." + manager + ".addInitProperty('" + key + "','" + value + "');";
    }
    private String wrapWithAnonymousScope(String script) {
        return ";(function() { " + script + "})();";
    }
    private String wrapWithManagerCheck(String script, String manager) {
        return "if (CQ_Analytics && CQ_Analytics." + manager + ") {" + script + "}";
    }

    @Override
    public AuthorizableResolution getAuthorizableResolution(SlingHttpServletRequest request) {
        final WCMMode wcmMode = WCMMode.fromRequest(request);

        if (wcmMode != null && !WCMMode.DISABLED.equals(wcmMode)) {
            // If in Author Mode
            final String authorizableId = getParameterOrAttribute(request, AUTHORIZABLE_ID, null);
            if (StringUtils.isNotBlank(authorizableId)) {
                log.debug("Use Impersonation");
                return AuthorizableResolution.IMPERSONATION;
            }
        }

        log.debug("Use Authentication");
        return AuthorizableResolution.AUTHENTICATION;
    }

    private static String getParameterOrAttribute(HttpServletRequest request, String key, String dfault) {
        String value = null;
        if (request == null) {
            return value;
        }

        if (StringUtils.isNotBlank(request.getParameter(key))) {
            value = request.getParameter(key);
        } else if (StringUtils.isNotBlank((String) request.getAttribute(key))) {
            value = (String) request.getAttribute(key);
        }

        if (StringUtils.isBlank(value)) {
            value = dfault;
        }

        return value;
    }
}
