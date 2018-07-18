package com.adobe.acs.samples.httpcache;

/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.adobe.acs.commons.httpcache.engine.HttpCacheEngine;
import com.adobe.acs.commons.httpcache.exception.HttpCacheKeyCreationException;
import com.adobe.acs.commons.httpcache.exception.HttpCachePersistenceException;
import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automated Memory Cache Flusher
 * <p>
 * Helps reducing maintenance by automatically flushing the ACS commons memory cache by implementing ResourceChangeListener.
 * </p>
 */
@Component(
        configurationPolicy = ConfigurationPolicy.OPTIONAL,
        immediate = true,
        property = {
                ResourceChangeListener.PATHS + "=/apps/acs-samples/components/structure/page/layout",
                ResourceChangeListener.PATHS + "=/content/acs-samples",
                ResourceChangeListener.CHANGES + "=ADDED",
                ResourceChangeListener.CHANGES + "=CHANGED",
                ResourceChangeListener.CHANGES + "=REMOVED",
        }
)
// @formatter:on
public class AutomatedLayoutPageInvalidator implements ResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(AutomatedLayoutPageInvalidator.class);
    private static final String PATH_SUFFIX = "/" + JcrConstants.JCR_CONTENT + ".content.html";

    private static final Pattern PATTERN = Pattern.compile("((/content/acs-samples/(headers|footers)/[a-zA-Z0-9_-]{1,99}))(.*)/(.*)");

    @Reference
    private HttpCacheEngine engine;

    @Override
    public void onChange(List<ResourceChange> changes) {
        for (ResourceChange change : changes) {

            LOG.debug("Attempting to extract header path from: {}", change.getPath());
            String layoutPagePath = extractHeaderPath(change.getPath());

            if (StringUtils.isNotEmpty(layoutPagePath)) {
                LOG.debug("Extracted header path: {}", layoutPagePath);
                try {
                    LOG.debug("Flushing path {}", layoutPagePath + PATH_SUFFIX);
                    engine.invalidateCache(layoutPagePath + PATH_SUFFIX);
                } catch (HttpCachePersistenceException | HttpCacheKeyCreationException e) {
                    LOG.error("Error flushing path!", e);
                }
            }

        }
    }

    private String extractHeaderPath(String headerChildResourcePath) {
        Matcher matcher = PATTERN.matcher(headerChildResourcePath);

        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}