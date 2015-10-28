package com.adobe.acs.samples.replication.impl;

import com.day.cq.replication.*;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;

@Component
@Service
public class SampleReplicatorImpl implements SampleReplicator {

    // Replicator OSGi service is needed to add dispatcher flush requests to the replication queue
    @Reference
    protected Replicator replicator;

    @Override
    public void replicate(ResourceResolver resourceResolver, String path, final String agentId,
                          ReplicationActionType replicationAction) throws ReplicationException {

        final ReplicationOptions options = new ReplicationOptions();

        // AgentFilter selects the replication agents that accept the replication request
        options.setFilter(new AgentFilter() {

            @Override
            public boolean isIncluded(Agent agent) {
                // Return true for those agents that should accept the replication request.
                // In this sample: explicitly filter by the agent's ID
                return agentId.equals(agent.getId());
            }
        });

        options.setSynchronous(false);
        options.setSuppressStatusUpdate(true);
        options.setSuppressVersions(true);

        replicator.replicate(resourceResolver.adaptTo(Session.class), replicationAction, path, options);
    }


}
