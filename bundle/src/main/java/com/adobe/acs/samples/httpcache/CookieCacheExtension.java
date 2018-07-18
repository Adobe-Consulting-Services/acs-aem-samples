package com.adobe.acs.samples.httpcache;

import com.adobe.acs.commons.httpcache.config.HttpCacheConfig;
import com.adobe.acs.commons.httpcache.config.HttpCacheConfigExtension;
import com.adobe.acs.commons.httpcache.exception.HttpCacheKeyCreationException;
import com.adobe.acs.commons.httpcache.exception.HttpCacheRepositoryAccessException;
import com.adobe.acs.commons.httpcache.keys.CacheKey;
import com.adobe.acs.commons.httpcache.keys.CacheKeyFactory;
import com.adobe.acs.samples.httpcache.definitions.CookieCacheExtensionConfig;
import com.adobe.acs.samples.httpcache.key.CookieCacheKey;
import com.adobe.acs.samples.httpcache.key.CookieKeyValueMap;
import com.adobe.acs.samples.httpcache.key.CookieKeyValueMapBuilder;
import com.google.common.collect.ImmutableSet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.util.Set;

/**
 * CookieCacheExtension
 * <p>
 * This extension on the HTTP cache allows for specific cookie combinations to create seperated cache entries.
 * This so we can present a different header based on cookie values, which tell us if a user is logged in and what type of user it is.
 * </p>
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE , service = {HttpCacheConfigExtension.class, CacheKeyFactory.class})
@Designate(ocd = CookieCacheExtensionConfig.class, factory = true)
public class CookieCacheExtension implements HttpCacheConfigExtension, CacheKeyFactory {
    private static final Logger log = LoggerFactory.getLogger(CookieCacheExtension.class);

    private boolean emptyAllowed;
    private String configName;
    private Set<String> cookieKeys;

    //-------------------------<HttpCacheConfigExtension methods>

    @Override
    public boolean accepts(SlingHttpServletRequest request, HttpCacheConfig cacheConfig) throws
            HttpCacheRepositoryAccessException {

        if(emptyAllowed){
            return true;
        }else{
            Set<Cookie> presentCookies = ImmutableSet.copyOf(request.getCookies());
            return containsAtLeastOneMatch(presentCookies);
        }
    }

    private boolean containsAtLeastOneMatch(Set<Cookie> presentCookies){
        CookieKeyValueMapBuilder builder = new CookieKeyValueMapBuilder(cookieKeys, presentCookies);
        CookieKeyValueMap map = builder.build();
        return !map.isEmpty();
    }


    //-------------------------<CacheKeyFactory methods>

    @Override
    public CacheKey build(final SlingHttpServletRequest slingHttpServletRequest, final HttpCacheConfig cacheConfig)
            throws HttpCacheKeyCreationException {

        ImmutableSet<Cookie> presentCookies = ImmutableSet.copyOf(slingHttpServletRequest.getCookies());
        CookieKeyValueMapBuilder builder = new CookieKeyValueMapBuilder(cookieKeys, presentCookies);
        return new CookieCacheKey(slingHttpServletRequest, cacheConfig, builder.build());
    }


    public CacheKey build(String resourcePath, HttpCacheConfig httpCacheConfig) throws HttpCacheKeyCreationException {
        return new CookieCacheKey(resourcePath, httpCacheConfig, new CookieKeyValueMap());
    }

    @Override
    public boolean doesKeyMatchConfig(CacheKey key, HttpCacheConfig cacheConfig) throws HttpCacheKeyCreationException {

        // Check if key is instance of GroupCacheKey.
        if (!(key instanceof CookieCacheKey)) {
            return false;
        }

        CookieCacheKey thatKey = (CookieCacheKey) key;

        return new CookieCacheKey(thatKey.getUri(), cacheConfig,thatKey.getKeyValueMap()).equals(key);
    }


    //-------------------------<OSGi Component methods>

    @Activate
    protected void activate(CookieCacheExtensionConfig config) {
        this.cookieKeys = ImmutableSet.copyOf(config.allowedCookieKeys());
        this.configName = config.configName();
        this.emptyAllowed = config.emptyAllowed();
        log.info("GroupHttpCacheConfigExtension activated/modified.");
    }
}
