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
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Session;
import java.util.Calendar;
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
 */

/** THIS SAMPLE TARGETS SLING MODELS v1.3+ **/

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        resourceType = "acs-samples/components/content/sling-model"
)
@Exporter(name = "jackson", extensions = "json", options = {
        /**
         * Jackson options:
         * - Mapper Features: http://static.javadoc.io/com.fasterxml.jackson.core/jackson-databind/2.8.5/com/fasterxml/jackson/databind/MapperFeature.html
         * - Serialization Features: http://static.javadoc.io/com.fasterxml.jackson.core/jackson-databind/2.8.5/com/fasterxml/jackson/databind/SerializationFeature.html
         */
        @ExporterOption(name = "MapperFeature.SORT_PROPERTIES_ALPHABETICALLY", value = "true"),
        @ExporterOption(name = "SerializationFeature.WRITE_DATES_AS_TIMESTAMPS", value="false")
})
/**
 * For Jackson Annotations: https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations
 */
public class SampleSlingModelExporter {

    @Self
    private Resource resource;

    // Inject a property name whose name does NOT match the Model field name
    // Since the Default inject strategy is OPTIONAL (set on the @Model), we can mark injections as @Required. @Optional can be used if the default strategy is REQUIRED.
    @Inject @Named("jcr:title") @Required
    private String title;

    // Inject a fields whose property name DOES match the model field name
    @Inject @Optional
    private String pageTitle;

    // Mark as Optional
    @Inject @Optional
    private String navTitle;

    // Provide a default value if the property name does not exist
    @Inject @Named("jcr:description") @Default(values = "No description provided")
    private String description;

    // Various data types can be injected
    @Inject @Named("jcr:created")
    private Calendar createdAt;

    @Inject
    boolean navRoot;

    // Inject OSGi services
    @Inject @Required
    private QueryBuilder queryBuilder;

    // Injection will occur over all Injectors based on Ranking;
    // Force an Injector using @Source(..)
    // If an Injector is not working; ensure you are using the latest version of Sling Models
    @Inject @Source("sling-object") @Required
    private ResourceResolver resourceResolver;

    // Internal state populated via @PostConstruct logic
    private long size;
    private Page page;

    @PostConstruct
    // PostConstructs are called after all the injection has occurred, but before the Model object is returned for use.
    private void init() {
        // Note that @PostConstruct code will always be executed on Model instantiation.
        // If the work done in PostConstruct is expensive and not always used in the consumption of the model, it is
        // better to lazy-execute the logic in the getter and persist the result in  model state if it is requested again.
        page = resourceResolver.adaptTo(PageManager.class).getContainingPage(resource);

        final Map<String, String> map = new HashMap<String, String>();
        // Injected fields can be used to define logic
        map.put("path", page.getPath());
        map.put("type", "cq:Page");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final SearchResult result = query.getResult();
        this.size = result.getHits().size();
    }

    /**
     * This getter wraps business logic around how an logic data point (title) is represented for this resource.
     *
     * @return The Page Title if exists, with fallback to the jcr:title
     */
    public String getTitle() {
        return StringUtils.defaultIfEmpty(pageTitle, title);
    }

    /**
     * This getter exposes data Injected into the Model and allows parameterized manipulation of the output.
     *
     * @param truncateAt length at which to truncate
     * @return the truncated description.
     */
    public String getDescription(int truncateAt) {
        return this.description.substring(0, truncateAt) + "...";
    }


    /**
     * Default implementation of the parameterizable getDescription(..).
     *
     * @return the truncated description.
     */
    public String getDescription() {
        return this.getDescription(100);
    }

    /**
     * This getter exposes the work of a @PostConstruct method.
     *
     * @return the number of cq:Pages that exist under this resource.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @return the created at Calendar value as Date.
     */

    // @JsonIgnore is a Jackson Annotation specific to this field that prevents this field from being serialized into the exported JSON.
    // For a list of Jackson Annotations see https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations
    @JsonIgnore
    public Calendar getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the resource path to this content. Does not include the extension.
     */
    public String getPath() {
        return page.getPath();
    }

    public String getHelloWorld() {
        return "Hello World";
    }

    // @JsonProperty is a Jackson Annotation specific to this field defines the JSON Property name to expose this method as.
    // For a list of Jackson Annotations see https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations
    @JsonProperty(value = "goodbye-world")
    public String goodbyeWorld() {
        return "Goodbye World";
    }
}