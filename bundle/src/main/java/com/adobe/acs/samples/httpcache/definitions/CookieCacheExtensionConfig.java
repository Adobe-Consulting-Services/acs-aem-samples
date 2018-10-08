package com.adobe.acs.samples.httpcache.definitions;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * CookieCacheExtensionConfig
 * <p>
 * Configuration OCD object for the CookiecCacheExtension
 * </p>
 */
@ObjectClassDefinition(
    name = "CookieCacheExtensionConfig - Configuration OCD object for the CookiecCacheExtension",
    description = "Extension for the ACS commons HTTP Cache. Leverages cookies."
)
public @interface CookieCacheExtensionConfig {

    @AttributeDefinition(
        name = "Configuration Name",
        description = "The unique identifier of this extension"
    )
    String configName() default "";

    @AttributeDefinition(
        name = "Allowed Cookies",
        description = "Cookie keys that will used to generate a cache key."
    )
    String[] allowedCookieKeys() default {};

    @AttributeDefinition(
        name = "Empty is allowed",
        description = "Cookie keys that will used to generate a cache key."
    )
    boolean emptyAllowed() default false;

}