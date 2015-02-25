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

package com.adobe.acs.samples.querybuilder.impl;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.AbstractPredicateEvaluator;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.PredicateEvaluator;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

/**
 * The QueryBuilder predicate for this Sample would be structured like so...
 *
 * type=cq:PageContent
 * path=/content
 *
 * json.property=myProp
 * json.key=type
 * json.value=featured
 *
 * `property` is the property name to locate the JSON String
 * `key` is the JSON key whose value will be matched again the value of `value`
 *
 * ex. /content/myPage/jcr:content@myProp={"type":"featured"}
 */
@Component(
        label = "ACS AEM Samples - Sample QueryBuilder JSON Predicate Evaluator",
        factory = "com.day.cq.search.eval.PredicateEvaluator/json"
)
@Service
public class SampleFilteringPredicateEvaluator extends AbstractPredicateEvaluator implements PredicateEvaluator {
    private static final Logger log = LoggerFactory.getLogger(SampleFilteringPredicateEvaluator.class);

    @Override
    public boolean canXpath(final Predicate predicate, final EvaluationContext context) {
        // This sample ONLY performs filtering on query search results; So return FALSE from "canXpath"
        // as this implementation does not provide and XPath to execute.

        // By default AbstractPredicateEvaluator.canXPath(..) returns true, so make sure you return false
        // else it will cause the includes(..) to be ignored.
        return false;
    }

    /* Post result set Filtering */

    @Override
    public boolean includes(final Predicate predicate, final Row row, final EvaluationContext evaluationContext) {
        // Return true to include the row in the result set
        // Return false to exclude the row from the result set

        // Get the QueryBuilder parameters

        final String propertyName = predicate.get("property");
        final String key = predicate.get("key");
        final String value = predicate.get("value");

        try {
            // Get the Node associated w the hit
            // Normally we'd use the Sling API (Resource/ValueMap) but performance is paramount in this case
            // So stick w the less elegant (to code) JCR Node APIs
            final Node node = row.getNode();

            if (node.hasProperty(propertyName)) {
                // Get the JSON string from the property
                final String jsonString = node.getProperty(propertyName).getString();

                if (StringUtils.isNotBlank(jsonString)) {
                    final JSONObject json = new JSONObject(jsonString);

                    // Get compare the predicate defined value (value) to the value of at the provided key in the
                    // JSON object; If true; we have a match and include
                    return StringUtils.equals(value, json.optString(key));
                }
            }
        } catch (RepositoryException e) {
            // Cannot get the Node so exclude!
            log.error("Could not convert a Result hit to a Node", e);
            return false;
        } catch (JSONException e) {
            // Value is not valid JSON so exclude; who knows what this is!
            log.warn("Property value is not JSON.", e);
            return false;
        }

        // Missing or un-matching data, so exclude
        return false;
    }

    @Override
    public boolean canFilter(final Predicate predicate, final EvaluationContext evaluationContext) {
        // Check if this predicate is capable of filtering results; This occurs once per application to a result set
        // and not PER result row

        // In this case; checks to ensure all predicate params are provided
        final String propertyName = predicate.get("property");
        final String key = predicate.get("key");
        final String value = predicate.get("value");

        final boolean filterable = StringUtils.isNotBlank(propertyName) && StringUtils.isNotBlank(key)
                && StringUtils.isNotBlank(value);

        log.info("Can filter w/ ACS AEM Samples JSON Predicate Evaluator: {}", filterable);

        return filterable;
    }

    @Override
    public boolean isFiltering(final Predicate predicate, final EvaluationContext context) {
        // .canFilter(..) has replaced isFiltering(..)
        return this.canFilter(predicate, context);
    }
}
