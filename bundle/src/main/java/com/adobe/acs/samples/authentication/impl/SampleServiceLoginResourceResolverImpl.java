package com.adobe.acs.samples.authentication.impl;

import com.adobe.acs.samples.SampleExecutor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

@Component(
    label = "ACS AEM Samples - Sample Login Resource Resolver",
    description = "A sample use of Sling's ResourceResolverFactory.getServiceResourceResolver(..)"
)
@Service
public class SampleServiceLoginResourceResolverImpl implements SampleExecutor {
    private static final Logger log = LoggerFactory.getLogger(SampleServiceLoginResourceResolverImpl.class);

    
    // This "identifier" does NOT directly correlate to a CRX User; The mapping of this value to the CRX User 
    // happens in the ServiceUserMapper OSGi config
    //
    // /apps/acs-samples/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.xml
    
    private static final String SERVICE_ACCOUNT_IDENTIFIER = "sample-service";
    
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

     /** try-with-resources and auto-closing ResourceResolver introduced in: https://issues.apache.org/jira/browse/SLING-4805 **/
    
     /**
     * This uses the try-with-resources approach and auto-closes the resource resolver.
     * Available in AEM 6.2+ and Java 7+.
     * 
     * Example for getting the Bundle Service User
     *
     * @return the user ID
     */
    private final String getBundleServiceUser() {
        // Get the auto-closing Service resource resolver
        try( ResourceResolver serviceResolver = resourceResolverFactory.getServiceResourceResolver(null)) {
            serviceResolver = ;

            if (serviceResolver != null) {
                // Do some work w your service resource resolver
                return serviceResolver.getUserID();
            } else {
                return "Could not obtain a User for Bundle Service";
            }
        }
    }
    
     /**
     * The try-catch-finally approach can be replaced with the try-with-resources approach defined above in AEM 6.2 and later and Java7+.
     * 
     * Example for getting the Bundle SubService User
     *
     * @return the user ID
     */
    private final String getSubServiceUser() {
        // Create the Map to pass in the Service Account Identifier
        // Remember, "SERVICE_ACCOUNT_IDENTIFIER" is mapped  to the CRX User via a SEPARATE ServiceUserMapper Factory OSGi Config
        final Map<String, Object> authInfo = Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE,
                    (Object) SERVICE_ACCOUNT_IDENTIFIER);

        // Get the auto-closing Service resource resolver        
        try (ResourceResolver serviceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo)) {
            if (serviceResolver != null) {
                // Do some work w your service resource resolver
                return serviceResolver.getUserID();
            } else {
                return "Could not obtain a User for the Service: " + SERVICE_ACCOUNT_IDENTIFIER;
            }
        } 
    }
    
    /** LEGACY TRY-CATCH-FINALLY EXAMPLES ARE BELOW **/

    /**
     * The try-catch-finally approach can be replaced with the try-with-resources approach defined above in AEM 6.2 and later and Java7+.
     * 
     * Example for getting the Bundle Service User
     *
     * @return the user ID
     */
    private final String getBundleServiceUser() {

        ResourceResolver serviceResolver = null;

        try () {
            // Get the Service resource resolver
            serviceResolver = resourceResolverFactory.getServiceResourceResolver(null);

            if(serviceResolver != null) {
                // Do some work w your service resource resolver

                return serviceResolver.getUserID();
            } else {
                return "Could not obtain a CRX User for Bundle Service";
            }
        } catch (LoginException e) {
            return "Login Exception when obtaining a User for the Bundle Service - " + e.getMessage();
        } finally {
            // As always, clean up any ResourceResolvers or JCR Sessions you open
            if (serviceResolver != null) {
                serviceResolver.close();
            }
        }
    }


    /**
     * The try-catch-finally approach can be replaced with the try-with-resources approach defined above in AEM 6.2 and later and Java7+.
     * 
     * Example for getting the Bundle SubService User
     *
     * @return the user ID
     */
    private final String getSubServiceUser() {

        ResourceResolver serviceResolver = null;

        try {
            // Create the Map to pass in the Service Account Identifier
            // Remember, "SERVICE_ACCOUNT_IDENTIFIER" is mapped  to the CRX User via a SEPARATE ServiceUserMapper Factory OSGi Config
            final Map<String, Object> authInfo = Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE,
                    (Object) SERVICE_ACCOUNT_IDENTIFIER);

            // Get the Service resource resolver
            serviceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo);

            if(serviceResolver != null) {
                // Do some work w your service resource resolver

                return serviceResolver.getUserID();
            } else {
                return "Could not obtain a CRX User for the Service: " + SERVICE_ACCOUNT_IDENTIFIER;
            }
        } catch (LoginException e) {
            return "Login Exception when obtaining a User for the Service: " + SERVICE_ACCOUNT_IDENTIFIER + " - " + e.getMessage();
        } finally {
            // As always, clean up any ResourceResolvers or JCR Sessions you open
            if (serviceResolver != null) {
                serviceResolver.close();
            }
        }
    }

    // This is a method to see what this sample returns
    public final String execute() {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.printf("Bundle Service User: %s", this.getBundleServiceUser()).println();
        pw.printf("Sub Service User: %s", this.getSubServiceUser()).println();

        return sw.toString();
    }

    @Override
    public String execute(ResourceResolver resourceResolver) {
        return execute();
    }
}
