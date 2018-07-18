package com.adobe.acs.samples.httpcache.key;

import com.adobe.acs.commons.httpcache.config.HttpCacheConfig;
import com.adobe.acs.commons.httpcache.exception.HttpCacheKeyCreationException;
import com.adobe.acs.commons.httpcache.keys.AbstractCacheKey;
import com.adobe.acs.commons.httpcache.keys.CacheKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * CookieCacheKey
 * <p>
 * This class represents a cache key in the HTTP Cache that will be used to separate header cache entries.
 * </p>
 */
public class CookieCacheKey extends AbstractCacheKey implements CacheKey {


    private final CookieKeyValueMap keyValueMap;

    public CookieCacheKey(SlingHttpServletRequest request, HttpCacheConfig cacheConfig, CookieKeyValueMap keyValueMap) throws
            HttpCacheKeyCreationException {

        super(request, cacheConfig);
        this.keyValueMap = keyValueMap;
    }

    public CookieCacheKey(String uri, HttpCacheConfig cacheConfig, CookieKeyValueMap keyValueMap) throws HttpCacheKeyCreationException {
        super(uri, cacheConfig);

        this.keyValueMap = keyValueMap;
    }

    public CookieKeyValueMap getKeyValueMap() {
        return keyValueMap;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        CookieCacheKey that = (CookieCacheKey) o;

        return new EqualsBuilder()
                .append(getUri(), that.getUri())
                .append(keyValueMap, that.keyValueMap)
                .append(getAuthenticationRequirement(), that.getAuthenticationRequirement())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUri())
                .append(keyValueMap)
                .append(getAuthenticationRequirement()).toHashCode();
    }

    @Override
    public String toString() {
        StringBuilder formattedString = new StringBuilder(this.uri);
        formattedString.append(keyValueMap.toString());
        formattedString.append("[AUTH_REQ:" + getAuthenticationRequirement() + "]");
        return formattedString.toString();
    }
}