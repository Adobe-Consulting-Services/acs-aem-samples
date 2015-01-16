package com.adobe.acs.samples.clientcontext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

public interface ClientContextStore {
    public static final String CONTEXT_STORE_ID = "contextstore.id";

    public static final String ANONYMOUS = ClientContextBuilder.ANONYMOUS;
    public static final String AUTHORIZABLE_ID = ClientContextBuilder.AUTHORIZABLE_ID;

    public String getContextStoreManagerName();

    public JSONObject getJSON(SlingHttpServletRequest request) throws JSONException;

    public boolean handleAnonymous();

    public JSONObject getAnonymousJSON(SlingHttpServletRequest request) throws JSONException;
}
