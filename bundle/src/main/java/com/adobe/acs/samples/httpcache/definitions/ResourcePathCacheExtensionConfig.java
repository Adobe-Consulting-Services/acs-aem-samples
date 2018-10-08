package com.adobe.acs.samples.httpcache.definitions;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * ResourcePathCacheExtensionConfig
 * <p>
 * Configuration OCD object for the ResourcePathCacheExtension
 * </p>
 */
@ObjectClassDefinition(
    name = "ResourcePathCacheExtensionConfig - Configuration OCD object for the ResourcePathCacheExtension",
    description = "Extension for the ACS commons HTTP Cache. Based on resource paths, selectors and extensions."
)
public @interface ResourcePathCacheExtensionConfig {

    @AttributeDefinition(
        name = "Configuration Name",
        description = "The unique identifier of this extension"
    )
    String configName() default "";

    @AttributeDefinition(
        name = "Resource path patterns",
        description = "List of resource path patterns (regex) that will be valid for caching"
    )
    String[] resourcePathPatterns();

    @AttributeDefinition(
        name = "Selector patterns",
        description = "List of selector patterns (regex) that will be valid for caching"
    )
    String[] selectors();

    @AttributeDefinition(
        name = "Extension patterns",
        description = "List of extension patterns (regex) that will be valid for caching"
    )
    String[] extensions();
}