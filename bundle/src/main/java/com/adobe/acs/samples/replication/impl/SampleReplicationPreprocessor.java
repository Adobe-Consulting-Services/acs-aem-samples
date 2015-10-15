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

import com.day.cq.replication.Preprocessor;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        label = "ACS AEM Samples - Sample Replication Preprocessor"
)
@Service
public class SampleReplicationPreprocessor implements Preprocessor {
    private static final Logger log = LoggerFactory.getLogger(SampleReplicationPreprocessor.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;


    @Override
    public void preprocess(final ReplicationAction replicationAction,
                           final ReplicationOptions replicationOptions) throws ReplicationException {

        if (replicationAction == null || !ReplicationActionType.ACTIVATE.equals(replicationAction.getType())) {
            // Do nothing
            return;
        }

        // Get the path of the replication payload
        final String path = replicationAction.getPath();

        ResourceResolver resourceResolver = null;

        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            // Get the payload as a resource; In this case getting the jcr:content node since we'll
            // be writing a custom value to it (this will fail if writing to cq:Page resource)
            final Resource resource = resourceResolver.getResource(path).getChild("jcr:content");

            if (resource == null) {
                // Remember; ALL replications go through this; so check to make sure that what
                // you're doing is Universal OR put your checks in early.
                log.warn("Could not find jcr:content node for resource to apply checksum!");
                return;
            }

            // Get the resource's properties for modification
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);

            // Apply some business logic; in this case we write a checksum based on some properties we care about
            properties.put("checksum", this.checksum(properties));

            resourceResolver.commit();
        } catch (LoginException e) {
            // To prevent Replication from happening, throw a ReplicationException
            throw new ReplicationException(e);
        } catch (PersistenceException e) {
            // To prevent Replication from happening, throw a ReplicationException
            throw new ReplicationException(e);
        } finally {
                if (resourceResolver != null && resourceResolver.isLive()) {
                // Always close resource resolver you open
                resourceResolver.close();
            }
        }
    }

    private Long checksum(final ValueMap properties) {
        // Stub method; Compute a checksum of certain properties values you care bout
        return 1L;
    }
}
