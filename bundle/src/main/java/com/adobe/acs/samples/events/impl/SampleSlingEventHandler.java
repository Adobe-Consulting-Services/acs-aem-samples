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

package com.adobe.acs.samples.events.impl;

import com.day.cq.replication.ReplicationAction;
import org.apache.commons.lang.ArrayUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.TopologyEventListener;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

@Component(
        label = "ACS AEM Samples - Sling Event Handler",
        description = "Sample implementation of a Custom Event Listener based on Sling",

        // One of the few cases where immediate = true; this is so the Event Listener starts listening immediately
        immediate = true
)
@Properties({
        @Property(
                label = "Event Topics",
                value = {"samples/events/poked", "samples/events/*"},
                description = "[Required] Event Topics this event handler will to respond to.",
                name = EventConstants.EVENT_TOPIC,
                propertyPrivate = true
        ),
        @Property(
                label = "Event Filters",
                value =   "(&(" + ReplicationAction.PROPERTY_TYPE + "=DEACTIVATE))",
                description = "[Optional] Event Filters used to further restrict this event handler; Uses LDAP expression against event properties.",
                name = EventConstants.EVENT_FILTER,
                propertyPrivate = true
        )
})
@Service
public class SampleSlingEventHandler implements EventHandler, TopologyEventListener {
    private static final Logger log = LoggerFactory.getLogger(SampleSlingEventHandler.class);

    private boolean isLeader = false;

    @Reference
    private JobManager jobManager;

    @Override
    public void handleEvent(final Event event) {
        boolean handleLocally = false;
        boolean handleWithLeader = !handleLocally;


        /**
         * Sling Event Properties - VERY handy
         *
         * This aren't guaranteed to have non-null values; so check before using.
         */

        // Resource path "undergoing" the event
        event.getProperty(SlingConstants.PROPERTY_PATH);

        // Resource type
        event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);

        // Resource super type
        event.getProperty(SlingConstants.PROPERTY_RESOURCE_SUPER_TYPE);

        // Properties names that were added/changes/removed
        event.getProperty(SlingConstants.PROPERTY_ADDED_ATTRIBUTES);
        event.getProperty(SlingConstants.PROPERTY_CHANGED_ATTRIBUTES);
        event.getProperty(SlingConstants.PROPERTY_REMOVED_ATTRIBUTES);

        // User id
        event.getProperty(SlingConstants.PROPERTY_USERID);

        if (!ArrayUtils.contains(event.getPropertyNames(), EventUtil.PROPERTY_DISTRIBUTE)) {
            // This is the check for a distributed event or not; if this property does not exist, it usually
            // means that this event handler should process the job, as no other event handlers
            // will see this event.

            // There must be a JobConsumer registered for this Topic
            jobManager.addJob("com/adobe/acs/samples/sample-job", new HashMap<String, Object>());

        } else if (handleLocally && EventUtil.isLocal(event)) {
            // This is a distributed event (first 'if' condition failed)

            // If this server created the event
            // then only this server should process the event

            // This will call this's process(..) method, passing in the event obj
            // JobUtil.processJob(..) sends/checks for an ack for this job

            // Jobs guarantee the event will be processed (though doesn't guarantee the job will be
            // processed SUCCESSFULLY)

            // There must be a JobConsumer registered for this Topic
            jobManager.addJob("com/adobe/acs/samples/sample-job", new HashMap<String, Object>());

        } else if (handleWithLeader && this.isLeader) {
            // This is a distributed event (first 'if' condition failed)

            // If a event is distributed, you may only want to execute it the Leader node in
            // the cluster.

            // There must be a JobConsumer registered for this Topic
            jobManager.addJob("com/adobe/acs/samples/sample-job", new HashMap<String, Object>());
        } else {
            // DO NOTHING!
        }
    }

    // This method makes this Listener cluster aware so it only responds to events on the Leader,
    // Without this every node in the cluster will process the event (generally resulting w duplicate work)
    @Override
    public void handleTopologyEvent(final TopologyEvent event) {
        if (event.getType() == TopologyEvent.Type.TOPOLOGY_CHANGED
                || event.getType() == TopologyEvent.Type.TOPOLOGY_INIT) {
            this.isLeader = event.getNewView().getLocalInstance().isLeader();
        }
    }
}
