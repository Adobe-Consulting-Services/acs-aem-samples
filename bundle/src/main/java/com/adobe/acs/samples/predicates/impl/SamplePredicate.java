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

package com.adobe.acs.samples.predicates.impl;


import com.day.cq.commons.predicate.AbstractNodePredicate;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

// http://docs.adobe.com/docs/en/cq/current/javadoc/com/day/cq/commons/predicate/AbstractNodePredicate.html

@Component(
        label = "ACS Samples - Sample Predicate",
        description = "Filtering Predicate for Resources/Nodes.")
@Properties({
        @Property(
                label = "Predicate Name",
                name = "predicate.name",
                value = "my-predicate",
                propertyPrivate = true
        )
})
@Service(value = Predicate.class)
public class SamplePredicate extends AbstractNodePredicate {
    private final static Logger log = LoggerFactory.getLogger(SamplePredicate.class);

    // Overriding evaluate(Object object) is optional. If this is not overridden AbstractNodePredicate
    // provides the following logic;
    //
    // * If the parameter "object" is a JCR Node OR Sling Resource that can be adapted to a JCR Node
    // * Then return evaluate(Node node)
    // * Else return false
    //
    // -----------------------------------------------------------------------------------------------------
    //
    // In the case where the object is not a Node or adaptable to a Node, for example: A synthetic resource
    // returned by a Sling Resource Provider, evaluate(Object object) can implement any custom logic as needed.

    @Override
    public final boolean evaluate(final Object object) {
        if (object instanceof SyntheticResource) {
            // If the object is a Synthetic Resource
            final Resource resource = (Resource) object;
            final ValueMap properties = resource.getValueMap();

            // Check the Synthetic Resource as needed to figure out if it should be filtered in or out.
            return StringUtils.equals(properties.get("cat", String.class), "meow");
        } else {
            // If not a SyntheticResource then use AbstractNodePredicate's "default" evaluation rules, which will
            // in turn call this.evaluate(Node node) defined below if the object is a/adaptable to a JCR Node.
            return super.evaluate(object);
        }
    }

    @Override
    public final boolean evaluate(final Node node) throws RepositoryException {
        // Anything can be checked here to file the Node in or out
        if (!node.isNodeType("cq:Page")) {
            // In this sample, only include cq:Page nodes
            return false;
        } else if (node.hasProperty("jcr:content/dog")) {
            // In this sample, only include cq:Page's that have a custom property set to a specific value
            String tmp = node.getProperty("jcr:content/dog").getString();
            return StringUtils.equals(tmp, "woof");
        }

        return false;
    }
}

