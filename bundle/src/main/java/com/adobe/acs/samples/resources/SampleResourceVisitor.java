/*
 * #%L
 * ACS AEM Samples
 * %%
 * Copyright (C) 2016 Adobe
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

package com.adobe.acs.samples.resources;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JavaDocs: https://docs.adobe.com/content/docs/en/cq/5-6-1/javadoc/index.html?org/apache/sling/api/resource/AbstractResourceVisitor.html
 *
 * The Resource Visitor walks the sub-tree of a provided resource.
 *
 * This can be and efficient way to look at huge numbers of resources.
 * Keep in mind this is best when the majority of the nodes under the tree should be evaluated and worked upon
 * rather then looking for needles in the haystack which is better served by querying against tailored Oak indexes.
 *
 * This can be called via like so...
 *
 * Resource rootResource = resourceResolver.getResource("/content/foo");
 *
 * SampleResourceVisitor srv = new SampleResourceVisitor("dam:Asset");
 *
 * srv.accept(rootResource);
 * srv.getCount();
 *
 */
public class SampleResourceVisitor extends AbstractResourceVisitor {
    private static final Logger log = LoggerFactory.getLogger(SampleResourceVisitor.class);

    private int count = 0;

    private String nodeType = "dam:Asset";

    /**
     * A custom cstor is not required but allows you to pass in state used by the visitor.
     * Common examples are passing in Writers that collect data written during the traversal.
     *
     * @param nodeType the node type to visit and perform work on. Also do not traverse nodes under this.
     */
    public SampleResourceVisitor(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Create getters as needed to expose state collected during the resource tree traversal.
     *
     * @return the number of acceptable resources visited
     */
    public final int getCount() {
        return this.count;
    }

    /**
     * The accept(..) method is the entry point to the visitor.
     *
     * Leveraging the provided accept(..) will traverse the entire resource tree.
     *
     * Overriding accept(..) is optional. But it can provide a handy way to reduce the traversal depth if you know
     * certain sub-trees aren't of interest. For example, if you are traversing and collecting all Assets, you know
     * that nothing under dam:Asset is of interest to you.
     *
     * @param resource the resource
     */
    @Override
    public void accept(final Resource resource) {
        // Don't try to traverse null resources
        if (resource == null) { return; }

        // Visit the resource to work; typically the check if work should be done is in visit(..) and not in here in
        // accept(..)
        this.visit(resource);

        // Check if the current resource's sub-tree should be traversed.
        if (!StringUtils.equals(this.nodeType,resource.getValueMap().get("jcr:primaryType", String.class))) {
            // in this case, the resource's type is not a dam:Asset (so its probably a sling:OrderFolder) so keep
            // traversing to look for dam:Assets
            this.traverseChildren(resource.listChildren());
        } else {
            // The resource is a dam:Asset so we know we dont need to traverse its children as this is the lowest
            // level we want to go.
        }
    }

    /**
     * This method is used to perform the work based on the visited resource. If possible exit quickly if no work
     * needs to be done to increase efficiency.
     * @param resource
     */
    @Override
    protected void visit(final Resource resource) {
        if (StringUtils.equals(this.nodeType,resource.getValueMap().get("jcr:primaryType", String.class))) {
            // Do some work with the resource... This can be anything.
            // In our case we simply increment our counter.
            this.count++;
        }
    }
}
