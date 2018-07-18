package com.adobe.acs.samples.httpcache;

import com.adobe.acs.commons.httpcache.config.HttpCacheConfig;
import com.adobe.acs.commons.httpcache.config.HttpCacheConfigExtension;
import com.adobe.acs.commons.httpcache.exception.HttpCacheKeyCreationException;
import com.adobe.acs.commons.httpcache.exception.HttpCacheRepositoryAccessException;
import com.adobe.acs.commons.httpcache.keys.CacheKey;
import com.adobe.acs.commons.httpcache.keys.CacheKeyFactory;
import com.adobe.acs.samples.httpcache.definitions.ResourcePathCacheExtensionConfig;
import com.adobe.acs.samples.httpcache.key.ResourcePathCacheKey;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ResourcePathCacheExtension
 * <p>
 * Simple cache extension to only create keys based on the resource path.
 * </p>
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE , service = {HttpCacheConfigExtension.class, CacheKeyFactory.class})
@Designate(ocd = ResourcePathCacheExtensionConfig.class, factory = true)
public class ResourcePathCacheExtension implements HttpCacheConfigExtension, CacheKeyFactory {

    private List<Pattern> resourcePathPatterns;
    private List<Pattern> selectorPatterns;
    private List<Pattern> extensionPatterns;

    private String configName;

    @Override
    public boolean accepts(SlingHttpServletRequest request, HttpCacheConfig cacheConfig) throws
            HttpCacheRepositoryAccessException {
        String resourcePath = request.getRequestPathInfo().getResourcePath();

        if(!matches(resourcePathPatterns, resourcePath)){
            return false;
        }

        if(!matches(selectorPatterns, request.getRequestPathInfo().getSelectorString())){
            return false;
        }

        if(!matches(extensionPatterns, request.getRequestPathInfo().getExtension())){
            return false;
        }

        return true;
    }

    private boolean matches(List<Pattern> source, String query) {
        if(StringUtils.isNotBlank(query)){
            for(Pattern pattern : source){
                if(pattern.matcher(query).find()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public CacheKey build(final SlingHttpServletRequest slingHttpServletRequest, final HttpCacheConfig cacheConfig)
            throws HttpCacheKeyCreationException {
        return new ResourcePathCacheKey(slingHttpServletRequest, cacheConfig);
    }


    public CacheKey build(String resourcePath, HttpCacheConfig httpCacheConfig) throws HttpCacheKeyCreationException {
        return new ResourcePathCacheKey(resourcePath, httpCacheConfig);
    }

    @Override
    public boolean doesKeyMatchConfig(CacheKey key, HttpCacheConfig cacheConfig) throws HttpCacheKeyCreationException {

        // Check if key is instance of GroupCacheKey.
        if (!(key instanceof ResourcePathCacheKey)) {
            return false;
        }

        ResourcePathCacheKey thatKey = (ResourcePathCacheKey) key;

        return new ResourcePathCacheKey(thatKey.getUri(), cacheConfig).equals(key);
    }

    @Activate
    protected void activate(ResourcePathCacheExtensionConfig config){
        this.configName = config.configName();
        this.resourcePathPatterns = compileToPatterns(config.resourcePathPatterns());
        this.extensionPatterns = compileToPatterns(config.extensions());
        this.selectorPatterns = compileToPatterns(config.selectors());
    }

    private List<Pattern> compileToPatterns(final String[] regexes) {
        final List<Pattern> patterns = new ArrayList<Pattern>();

        for (String regex : regexes) {
            if (StringUtils.isNotBlank(regex)) {
                patterns.add(Pattern.compile(regex));
            }
        }

        return patterns;
    }
}
