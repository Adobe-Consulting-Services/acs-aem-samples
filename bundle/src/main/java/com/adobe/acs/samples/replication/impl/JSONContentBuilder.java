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

package com.adobe.acs.samples.replication.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.jcr.JsonItemWriter;

import com.day.cq.replication.ContentBuilder;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;

/**
 * Transforms replicated data to a JSON payload for replication to external systems.
 */
@Component(label = "ACS AEM Samples - Sample Replication Content Builder")
@Service
@Properties({
    @Property(name = "name", value = "JSON")
})
public class JSONContentBuilder implements ContentBuilder {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public ReplicationContent create(Session sn, ReplicationAction action,
            ReplicationContentFactory rcf) throws ReplicationException {
        return create(sn, action, rcf, null);
    }

    @Override
    public ReplicationContent create(Session sn, ReplicationAction replicationAction,
            ReplicationContentFactory rcf, Map<String, Object> map)
            throws ReplicationException {

        if (replicationAction == null || !ReplicationActionType.ACTIVATE.equals(replicationAction.getType())) {
            // Do nothing, since a payload is only needed for ACTIVATE events
            return ReplicationContent.VOID;
        }

        // Get the path of the replication payload
        String path = replicationAction.getPath();

        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

            // Get the payload as a resource and adapt to a node;
            // We only want the jcr:content, otherwise descendant pages will be included in the JSON
            Resource resource = resourceResolver.getResource(path + "/jcr:content");
            Node node = resource.adaptTo(Node.class);

            // Create a temporary file to write the JSON representation to
            File outputFile = File.createTempFile("aem-", ".json");
            OutputStream outputStream = new FileOutputStream(outputFile);
            Writer writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            // Construct the JsonItemWriter with no properties to ignore;
            // maxRecursionDepth < 0 will allow for infinite recursion
            JsonItemWriter jsonItemWriter = new JsonItemWriter(null);
            jsonItemWriter.dump(node, writer, -1);

            // Create and return the ReplicationContent
            return rcf.create("application/json", outputFile, true);
        } catch (Exception e) {
            throw new ReplicationException(e);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                // Always close resource resolver you open
                resourceResolver.close();
            }
        }
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getTitle() {
        return "JSON for Replication to External Integrations";
    }
}
