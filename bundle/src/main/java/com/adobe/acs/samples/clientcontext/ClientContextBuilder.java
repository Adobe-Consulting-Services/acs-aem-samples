package com.adobe.acs.samples.clientcontext;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

public interface ClientContextBuilder {
    public static final String ANONYMOUS = ClientContextStore.ANONYMOUS;
    public static final String AUTHORIZABLE_ID = "authorizableId";
    public static final String XSS_SUFFIX = "_xss";
    public static final String PATH = "path";

    public enum AuthorizableResolution {
        AUTHENTICATION, // Publish
        IMPERSONATION   // Author
    }

    public String getAuthorizableId(SlingHttpServletRequest request);

    public String getPath(SlingHttpServletRequest request);

    public JSONObject getJSON(SlingHttpServletRequest request, ClientContextStore store) throws JSONException;

    public JSONObject xssProtect(JSONObject json, String... whiteList) throws JSONException;

    public boolean isSystemProperty(String key);

    public String getGenericInitJS(SlingHttpServletRequest request, ClientContextStore store) throws JSONException;

    public String getInitJavaScript(JSONObject json, ClientContextStore store);

    public String getInitJavaScript(JSONObject json, String manager);

    public AuthorizableResolution getAuthorizableResolution(SlingHttpServletRequest request);

    public ResourceResolver getResourceResolverFor(final String authorizableId) throws LoginException;

    public void closeResourceResolverFor(ResourceResolver resourceResolver);
}
