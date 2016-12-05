package com.adobe.acs.samples.events.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.observation.ExternalResourceChangeListener;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.event.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Sling Resource Change Listener is the preferred method for listening for Resource Change events in AEM.
 * This is preferred over the Sling Resource Event Listener, or the JCR Event Handler approaches.
 *
 * ResourceChangeListener Javadoc:
 *  - https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/org/apache/sling/api/resource/observation/ResourceChangeListener.html
 *
 * Note: To listen for External events, implements the ExternalResourceChangeListener. If ONLY local events are in scope, implement only the ResourceChangeListener.
 */
@Component(
        label = "ACS AEM Samples - Resource Change Listener",
        description = "A sample implementation of the Sling Resource Change Listener",
        metatype = true
)
@Properties({
        // Scope the paths as tightly as possible based on your use-case.
        @Property(
                label = "Paths",
                description = "[ Required ] A list of resource paths this listener will listen for change events.",
                name = ResourceChangeListener.PATHS,
                value = {"/content"}
        ),
        // Scope the types as tightly as possible based on your use-case.
        // If This property is not provided, ALL ChangeTypes will be accepted.
        // Available values are defined on: ResourceChange.ChangeType
        @Property(
                label = "Change Types",
                description = "[ Optional ] The change event types this listener will listener for. ",
                name = ResourceChangeListener.CHANGES,
                value = {"ADDED", "CHANGED", "REMOVED", "PROVIDER_ADDED", "PROVIDER_REMOVED"}
        )
})
@Service
public class SampleResourceChangeListener implements ResourceChangeListener, ExternalResourceChangeListener {
    private static final Logger log = LoggerFactory.getLogger(SampleResourceChangeListener.class);

    @Reference
    private JobManager jobManager;

    public void onChange(@Nonnull List<ResourceChange> changes) {
        // Iterate over the ResourceChanges and process them

        for (final ResourceChange change : changes) {
            // Process each change quickly; Do not do long-running work in the Resource Change Listener.
            // If expensive/long-running work is required to handle the event, create a Sling Job to perform that work.

            if (change.isExternal()) {
                // Since this implements BOTH the ResourceChangeListener AND ExternalResourceChangeListener
                // we can conditionally handle local vs external events.
            }

            switch (change.getType()) {
                case ADDED:
                    log.debug("Change Type ADDED: {}", change);
                    if (change.getAddedPropertyNames().contains("someProperty")) {
                        // Do some work
                        // In this case we will pass some some data from the Event to a custom job via a custom Job topic.
                        final Map<String, Object> props = new HashMap<String, Object>();
                        props.put("path", change.getPath());
                        props.put("userId", change.getUserId());
                        jobManager.addJob("com/adobe/acs/commons/samples/somePropertyAdded", props);
                    }
                    break;
                case CHANGED:
                    log.debug("Change Type CHANGED: {}", change);
                    if (change.getAddedPropertyNames().contains("someOtherProperty")) {
                        // Do some other work
                    }
                    break;
                case REMOVED:
                    log.debug("Change Type REMOVED: {}", change);
                    // etc.
                    break;
                case PROVIDER_ADDED:
                    log.debug("Change Type PROVIDER_ADDED: {}", change);
                    // etc.
                    break;
                case PROVIDER_REMOVED:
                    log.debug("Change Type PROVIDER_REMOVED: {}", change);
                    // etc.
                    break;
                default:
                    // Do nothing
            }
        }
    }
}
