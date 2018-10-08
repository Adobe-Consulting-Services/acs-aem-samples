package com.adobe.acs.samples.httpcache.key;

import javax.servlet.http.Cookie;
import java.util.Set;

/**
 * CookieKeyValueMapBuilder
 * <p>
 * The purpose of the cookie key value map builder is to handle cookiekeyvaluemap instantation.
 * </p>
 */
public class CookieKeyValueMapBuilder {

    private final Set<String> allowedKeys;
    private final Set<Cookie> presentCookies;
    private final CookieKeyValueMap keyValueMap = new CookieKeyValueMap();

    public CookieKeyValueMapBuilder(Set<String> allowedKeys, Set<Cookie> presentCookies){

        this.allowedKeys = allowedKeys;
        this.presentCookies = presentCookies;
    }


    public CookieKeyValueMap build(){

        presentCookies.stream()
                .filter( cookie -> allowedKeys.contains(cookie.getName()))
                .forEach(cookie -> keyValueMap.put(cookie.getName(), cookie.getValue()));

        return keyValueMap;
    }

}
