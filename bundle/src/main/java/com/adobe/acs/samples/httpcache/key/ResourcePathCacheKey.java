package com.adobe.acs.samples.httpcache.key;

import com.adobe.acs.commons.httpcache.config.HttpCacheConfig;
import com.adobe.acs.commons.httpcache.keys.AbstractCacheKey;
import com.adobe.acs.commons.httpcache.keys.CacheKey;
import com.day.cq.commons.PathInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;

/**
 * ResourcePathCacheKey
 * <p>
 * A key that only contains a resource path
 * </p>
 */
public class ResourcePathCacheKey  extends AbstractCacheKey implements CacheKey {


    private final String selector;
    private final String extension;

    public ResourcePathCacheKey(SlingHttpServletRequest request, HttpCacheConfig cacheConfig) {
        super(request, cacheConfig);

        RequestPathInfo pathInfo = request.getRequestPathInfo();
        selector =      pathInfo.getSelectorString();
        extension     = pathInfo.getExtension();
    }

    public ResourcePathCacheKey(String uri, HttpCacheConfig cacheConfig){
        super(uri, cacheConfig);
        RequestPathInfo pathInfo = new PathInfo(uri);
        selector =      pathInfo.getSelectorString();
        extension     = pathInfo.getExtension();
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        ResourcePathCacheKey that = (ResourcePathCacheKey) o;
        return new EqualsBuilder()
                .append(resourcePath, that.resourcePath)
                .append(getExtension(), that.getExtension())
                .append(getSelector(), that.getSelector())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(resourcePath)
                .toHashCode();
    }

    @Override
    public String toString(){
        return resourcePath + "." + getSelector() + "." + getExtension();
    }


    public String getSelector() {
        return selector;
    }

    public String getExtension() {
        return extension;
    }
}
