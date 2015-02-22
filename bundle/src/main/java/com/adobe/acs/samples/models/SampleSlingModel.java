/*
 * #%L
 * ACS AEM Samples
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

package com.adobe.acs.samples.models;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Source;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * Excellent documentation: http://sling.apache.org/documentation/bundles/models.html
 *
 * For the Sling Models to be picked up; Ensure the packages containing Sling Models are listed in the Bundle pom.xml
 *
 * <Sling-Model-Packages>
 *    com.adobe.acs.samples.models
 * </Sling-Model-Packages>
 *
 * ************************************************************************
 *
 *  The "adaptables" for a Sling Model is key element.
 *
 *  All the injected fields are looked up via a set of Injectors
 *  > http://sling.apache.org/documentation/bundles/models.html#available-injectors
 *  > Ensure you are using the latest Sling Model API and Impl bundles for access
 *    to the latest and greatest Injectors
 *
 */
@Model(adaptables=Resource.class)
public class SampleSlingModel {

    // Inject a property whose name does NOT match the Model field name
    @Inject @Named("jcr:title")
    private String title;

    // Inject a property whose name DOES match the Model field name
    @Inject @Default(values = "No description provided")
    private String description;

    // Set a default value
    @Inject @Optional
    private String theme;

    // Inject OSGi services
    @Inject
    private QueryBuilder queryBuilder;

    // Injection will occur over all Injectors based on Ranking;
    // Force an Injector using @Source(..)
    // If an Injector is not working; ensure you are using the latest version of Sling Models
    @Inject @Source("sling-object")
    private ResourceResolver resourceResolver;

    // Some other internal Model state
    private long totalMatches;

    @PostConstruct
    private void calculateTotalMatches() {
        // This is called after all the injection has occurred
        final Map<String, String> map = new HashMap<String, String>();
        map.put("path", "/content");
        map.put("type", "cq:Page");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final com.day.cq.search.result.SearchResult result = query.getResult();
        this.totalMatches = result.getTotalMatches();
    }

    // This getter exposes data Injected into the Model
    public String getDescription(int truncateAt) {
        return this.description.substring(0, truncateAt) + "...";
    }

    // This getter exposes the work of a @PostConstruct method
    public long getTotalMatches() {
        return this.totalMatches;
    }
}
