package com.adobe.acs.samples.events;

import com.adobe.acs.samples.SampleExecutor;
import com.day.cq.commons.jcr.JcrObservationThrottle;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.UUID;

/**
 * JavaDocs: https://docs.adobe.com/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/commons/jcr/JcrObservationThrottle.html
 *
 * JcrObservationThrottle (introduced way back in 5.x) allows for the queuing up of events and releasing them when all your work is done.
 * This can help prevent overly eager listeners from processing changes before the full set of changes is complete.
 */
public class SampleJcrObservationThrottle implements SampleExecutor {
    private static final Logger log = LoggerFactory.getLogger(SampleJcrObservationThrottle.class);

    @Override
    public String execute(ResourceResolver resourceResolver) {

        JcrObservationThrottle jcrObservationThrottle = null;
        try {
            // Create the unique, temp node under which the event queue will be stored.
            // Your resourceResolver must have permissions to create / modify / delete this node.

            final String tmpPath = "/tmp/jcr-event-throttle/" + this.getClass().getName() + "/" + UUID.randomUUID().toString();
            final Node tmpNode = JcrUtils.getOrCreateByPath(tmpPath, JcrConstants.NT_UNSTRUCTURED, resourceResolver.adaptTo(Session.class));

            // Create a new JcrObservationThrottle for this operation and pass in the tmpNode that will record/queue the events
            jcrObservationThrottle = new JcrObservationThrottle(tmpNode);

            // Start listening against this JCR Session
            jcrObservationThrottle.open();

            for (int i = 0; i < 10000; i++) {
                // Do a bunch of writes to the JCR
                if (i % 10 == 0) {
                    // You can perform multiple saves and "trap" the events in this throttle
                    resourceResolver.commit();
                }
            }

            if (resourceResolver.hasChanges()) {
                // Commit and lagging commits from the work loop above
                resourceResolver.commit();
            }

            log.debug("Waiting for throttled observation events to be delivered.");
            // *** WARNING ***
            // The amount of time to wait is determined by the event activity of the system at this point in time, so be conscious of this.
            final long t = jcrObservationThrottle.waitForEvents();
            log.info("Waited [ {} ] ms for throttled observation events to be delivered", t);
        } catch (RepositoryException | PersistenceException e) {
            log.error("Something bad happened!", e);
        } finally {
            if (jcrObservationThrottle != null) {
                // Always close the JcrObervationThrottle in a finally black to ensure it gets closed.
                jcrObservationThrottle.close();
            }
        }

        return null;
    }

    @Override
    public String execute() {
        throw new UnsupportedOperationException("Use the method that takes the resource resolver");
    }
}
