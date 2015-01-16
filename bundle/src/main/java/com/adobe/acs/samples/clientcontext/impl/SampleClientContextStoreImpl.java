package com.adobe.acs.samples.clientcontext.impl;

import com.adobe.acs.samples.clientcontext.ClientContextBuilder;
import com.adobe.acs.samples.clientcontext.ClientContextStore;
import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;
import com.adobe.granite.security.user.UserPropertiesService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

@Component(
        label = "ACS-AEM-Samples - Client Context Store",
        description = "ACS-AEM-Samples implementation of a service.",
        metatype = false,
        immediate = true,
        policy = ConfigurationPolicy.REQUIRE
)
@Properties({
        @Property(
                label = "Context Store ID",
                description = "This value is used to select the appropriate ClientContextStore implementation to support building out custom Client Contexts. Filter implementation: (contextstore.id=sample)",
                name = ClientContextStore.CONTEXT_STORE_ID,
                value = "sample",
                propertyPrivate = true
        ),
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        )
})
@Service
public class SampleClientContextStoreImpl implements ClientContextStore {
    private static final Logger log = LoggerFactory.getLogger(SampleClientContextStoreImpl.class);

    protected static final String DATA_MANAGER_NAME = "SampleDataMgr";

    @Reference
    private UserPropertiesService userPropertiesService;

    @Reference
    private ClientContextBuilder clientContextBuilder;

    /**
     * Creates and returns the JSON object responsible for populating the Client Context store.
     *
     * @param request
     * @return
     * @throws org.apache.sling.commons.json.JSONException
     */
    @Override
    public JSONObject getJSON(SlingHttpServletRequest request) throws JSONException {
        final String authorizableId = clientContextBuilder.getAuthorizableId(request);
        final UserProperties properties = getData(request.getResourceResolver(), authorizableId);

        /** Created JSON object **/

        JSONObject json = new JSONObject();

        json.put(AUTHORIZABLE_ID, authorizableId);

        json.put("context-store", "sample");

        json.put("key1", "logged in");
        json.put("key2", "known Surfer");
        json.put("key3", "/home/users/the-dude");

        return clientContextBuilder.xssProtect(json);
    }
    /**
     * Returns JSON specific for anonymous users. This is usually a pre-set or hardcoded set of values, and derived differently than those for authenticated users.
     *
     * @param request
     * @return JSON specific for anonymous users.
     * @throws JSONException
     */
    @Override
    public JSONObject getAnonymousJSON(SlingHttpServletRequest request) throws JSONException {
        JSONObject json = new JSONObject();

        json.put(AUTHORIZABLE_ID, ANONYMOUS);

        json.put("context-store", "sample");

        json.put("key1", "not logged in");
        json.put("key2", "unknown Surfer");
        json.put("key3", "/home/users/a/anonymous");

        return clientContextBuilder.xssProtect(json);
    }
    /**
     * Used to determine if getAnonymousJSON(..) should be called to generate JSON for requests deemed anonymous (either via Authentication or "impersonation" on Author)
     * <p/>
     * If only getJSON(..) is responsible for generating Client Context Store data, then return false from this method.
     *
     * @return true if this Client Context store implements a non-null getAnonymousJSON(..)
     */
    @Override
    public boolean handleAnonymous() {
        return true;
    }

    /**
     * Returns the name of the JS Data Manager that will will be loaded from this Client Context Store implemenation.
     * - An example value: SampleDataMgr
     *
     * @return the name of the Context Store Manager (Ex. SampleDataMgr).
     */
    @Override
    public String getContextStoreManagerName() {
        return DATA_MANAGER_NAME;
    }

    /**
     * @param resourceResolver
     * @param authorizableId
     * @return
     * @throws RepositoryException
     */
    private UserProperties getData(ResourceResolver resourceResolver, String authorizableId){
        UserProperties properties = null;
        try {
        final UserPropertiesManager userPropertiesManager =
                userPropertiesService.createUserPropertiesManager(resourceResolver);




            properties = userPropertiesManager.getUserProperties(authorizableId, "custom/store");
        } catch (RepositoryException ex) {
           log.error(ex.getMessage(),ex);
        }

        return properties;
    }
}
