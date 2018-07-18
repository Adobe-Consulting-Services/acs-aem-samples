package com.adobe.acs.samples.httpcache.key;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * CookieKeyValueMap
 * <p>
 * Basically a HashMap with a nice toString function for the CookieCacheKey to hold cookies into.
 * </p>
 */
public class CookieKeyValueMap extends HashMap<String,String> {

    public CookieKeyValueMap() {
    }


    @Override
    public String toString() {

        StringBuilder result = new StringBuilder("[CookieKeyValues:");

        Iterator<Entry<String,String>> entries = entrySet().iterator();

        if(!isEmpty()){
            while (entries.hasNext()) {

                Entry<String, String> entry = entries.next();
                String key = entry.getKey();
                String value = entry.getValue();

                if (StringUtils.isNotEmpty(value)) {
                    result.append(key + "=" + value);
                } else {
                    //cookie is only present, but no value.
                    result.append(key);
                }

                if (entries.hasNext()) {
                    result.append(",");
                }

            }
        }

        result.append("]");
        return result.toString();

    }
}
