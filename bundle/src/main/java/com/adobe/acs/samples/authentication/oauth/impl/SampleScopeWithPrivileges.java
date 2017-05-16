package com.adobe.acs.samples.authentication.oauth.impl;

import com.adobe.granite.oauth.server.Scope;
import com.adobe.granite.oauth.server.ScopeWithPrivileges;
import org.apache.jackrabbit.api.security.user.User;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * OAuth Scope support was introduced in AEM 6.3
 */
@Component(
        service = Scope.class
)
// This class must implement ScopeWithPrivileges, but it must register as an OSGi Service against Scope.class
public class SampleScopeWithPrivileges implements ScopeWithPrivileges {
    public static final String WRITE_DAM_SCOPE_NAME = "vendor-x__write-dam";
    public static final String BASE_PATH = "/content/dam";

    /**
     * Informational purposes only
     **/
    public String getDescription(HttpServletRequest request) {
        return "Write access to AEM Assets";
    }

    /**
     * return the unique Scope name. This value must be unique across all scope implementations.
     **/
    public String getName() {
        // If there is overlay in Scope's w the same `getName()` value, one of the named scopes will be selected at random for use (based on Service registration order).
        // If a scope is being provided as a 3rd party package, it is good to ensure the scope name has some low-likelihood collision name:
        // * For example: "vendor-x__dam_write"
        return WRITE_DAM_SCOPE_NAME;
    }

    /**
     * @param user The authenticated "AEM user" being asked to authorise the scope.
     *             return the JCR path these privileges provided by `getPrivileges()`.
     **/
    public String getResourcePath(User user) {
        // While the User is provided; it is atypical to derive the path based on the user.
        // Assuming a low number of path/privilege permutation is its usually better create multiple scopes for each user-type/path combination.

        // A use case for having the user drive the result of getResourcePath, is for a scope that provides access to the authorizing user's rep:User/profile node.
        return BASE_PATH;
    }

    /**
     * If the scope is associated with one specific endpoint return the URI to the endpoint. Otherwise return null.
     **/
    public String getEndpoint() {
        // Return null
        return null;
    }

    /**
     * - JCR Privileges: http://jackrabbit.apache.org/oak/docs/apidocs/org/apache/jackrabbit/oak/spi/security/privilege/PrivilegeConstants.html
     * - JCR Privilege Mapping: https://jackrabbit.apache.org/oak/docs/security/privilege/mappingtoitems.html
     * - AEM Privileges: cq:storeUGC, crx:replicate
     * - Custom privileges also supported (though these are rare)
     **/
    private static final String[] privileges = {
            "crx:replicate",
            "jcr:lockManagement",
            "jcr:versionManagement",
            "rep:write"
    };

    /**
     * return the privileges to be applied to the path returned by `getResource(...)`. Note these will supersede any JCR-based ACLs.
     **/
    public String[] getPrivileges() {
        return privileges;
    }
}