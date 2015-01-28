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

import com.day.cq.search.Query;
import com.day.cq.search.result.Hit;
import com.day.cq.search.writer.ResultHitWriter;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

import javax.jcr.RepositoryException;

/*
    Interface documentation: http://dev.day.com/docs/en/cq/current/javadoc/com/day/cq/search/writer/ResultHitWriter.html

    ResultHitWriter writes a search result Hit into a JSONWriter.
    This is used in the standard servlet for the query builder to allow different output renderings.

    The appropriate ResultHitWriter is selected by passing the desired name
    in the query using p.hitwriter=NAME as request parameter.

    Implementations of this interface must be defined as an OSGi component factory.
    The name of the factory must be the fully qualified name of this interface plus "/" and a distinct short name of
    the renderer (that will be used in request parameters to select it, NAME above).

    An example call to this Sample HitWriter might look like:

        http://localhost:4502/bin/querybuilder.json?...&p.hitwriter=acs-commons-sample

    Resulting JSON object would look like:

        [
            ...,
            {
                "path": "/content/path/to/hit",
                "key-to-use-in-json": "Hit Title (pulled from jcr:content node)",
                "complex": "Hello World"
            },
            ...
        ]
*/
@Component(
        label = "ACS AEM Samples - QueryBuilder ResultHitWriter",
        factory = "com.day.cq.search.writer.ResultHitWriter/acs-aem-commons-sample"
        // factory suffix defines the name which this hit writer is registered under (acs-aem-commons-sample)
)
public class SampleJsonHitWriter implements ResultHitWriter {

    @Override
    public final void write(Hit hit, JSONWriter jsonWriter, Query query) throws RepositoryException, JSONException {
        // This example assume the "hit" represents a [cq:Page] node (and not the [cq:Page]/jcr:content node)

        // The Resource that represents a Query "hit" (result); This can used to access other related resources in the JCR.
        final Resource resource = hit.getResource();
        
        // The Hit object contains the ValueMap representing the "hit" resource. 
        // This can be used to quickly get properties/relative properties from the hit to expose via the HitWriter.
        final ValueMap properties = hit.getProperties();
    
        // Write simple values like the node's path to the JSON result object
        jsonWriter.key("path").value(resource.getPath());

        // Write resource properties from the hit result node (or relative nodes) to the JSON result object
        // You have full control over the names/values of the JSON key/value pairs returned.
        // These do not have to match node names
        jsonWriter.key("key-to-use-in-json").value(properties.get("jcr:content/jcr:title", "") 
            + "(pulled from jcr:content node)");

        // Custom logic can be used to transform and/or retrieve data to be added to the resulting JSON object
        // Note: Keep this logic as light as possible. Complex logic can introduce performance issues that are
        // less visible (Will not appear in JMX Slow Query logs as this logic executes after the actual Query returns).
        String complexValue = sampleComplexLogic(resource);
        jsonWriter.key("complex").value(complexValue);
    }

    private String sampleComplexLogic(Resource resource) {
        // Perform any custom logic you want based on the hit resource; This could be "sub-queries",
        // or combining/scrubbing data.
        return "Hello World";
    }
}
