package com.adobe.acs.samples.replication.impl;

import aQute.bnd.annotation.ProviderType;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import org.apache.sling.api.resource.ResourceResolver;

@ProviderType
public interface SampleReplicator {

    /**
     * Explicitly replicates content using the given replication agent
     *
     * @param resourceResolver ResourceResolver instance
     * @param path The content path to replicate. Must match the path in dispatcher cache directory
     * @param agentId Agent ID of a  replication agent
     * @param replicationAction The replication action: ACTIVATE, DEACTIVATE, ..
     * @throws ReplicationException
     */
    public void replicate(ResourceResolver resourceResolver, String path, String agentId,
                          ReplicationActionType replicationAction) throws ReplicationException;


    // @dgonzalez wdyt ... more samples like "flushDispatcher()" ... "reverseReplicate()" .. "activate()" .. "deactivate()"

}
