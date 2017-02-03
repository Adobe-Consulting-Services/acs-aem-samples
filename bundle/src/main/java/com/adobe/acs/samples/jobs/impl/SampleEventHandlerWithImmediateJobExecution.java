package com.adobe.acs.samples.jobs.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@Component
@Properties({
        /**
         * Use the normal @Property annotations to define which event topics to listen to, and filter in events
         */
        @Property(
            label = "Event Topics",
            value = {SlingConstants.TOPIC_RESOURCE_CHANGED},
            description = "[Required] Event Topics this event handler will to respond to.",
            name = EventConstants.EVENT_TOPIC,
            propertyPrivate = true
        ),
        /* Event filters support LDAP filter syntax and have access to event.getProperty(..) values */
        /* LDAP Query syntax: https://goo.gl/MCX2or */
        @Property(
                label = "Event Filters",
                // Only listen on events associated with nodes that end with /jcr:content
                value =   "(path=*/jcr:content)",
                description = "[Optional] Event Filters used to further restrict this event handler; Uses LDAP expression against event properties.",
                name = EventConstants.EVENT_FILTER,
                propertyPrivate = true
        )
})
@Service
public class SampleEventHandlerWithImmediateJobExecution implements EventHandler {
    private static final Logger log = LoggerFactory.getLogger(SampleEventHandlerWithImmediateJobExecution.class);

    // Since this is a back-end service, its likely you will need to interact w the JCR. To do this, use the
    // ResourceResolverFactory to get the appropriate service user for this job.
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    // The Sling Scheduler will be used to create an immediate job
    @Reference
    private Scheduler scheduler;

    public void handleEvent(Event event) {
        /* NOTE: HANDLE EVENT MUST COMPLETE QUICKLY ELSE THE EVENT HANDLER MAY BE BLACKLISTED */

        // properties passed along w the event can be retrieved.
        // Apache Felix shows events and their properties: http://localhost:4502/system/console/events
        String path = (String) event.getProperty("path");

        // Perform any fast checks here that you cant perform via Event Filtering above

        // Create schedule options that executes immediately (aka NOW())
        ScheduleOptions options = scheduler.NOW();
        // Assign a "unique" name for this job

        String jobName = this.getClass().getSimpleName().toString().replace(".", "/") + "/" + path;
        // This jobName would be like: com/adobe/acs/samples/jobs/impl/SampleEventHandlerWithImmediateJobExecution/content/my-site/some-page/jcr:content
        options.name(jobName);

        // Note the job name is what determines is a job is considered running concurrently.
        // In this case, we do not want to process the same resource at the same time, so the jobName
        options.canRunConcurrently(false);

        // Create a new job object (must be runnable).
        ImmediateJob job = new ImmediateJob(path);

        // Create and schedule the job
        scheduler.schedule(job, options);
    }

    /**
     * ImmediateJob is an inner class that implements Runnable.
     *
     * The benefit of making this an inner class is it allows access ot OSGi services @Reference'd, and consolidates
     * the logic into a single file.
     */
    private class ImmediateJob implements Runnable {
        private final String path;

        /**
         * The constructor can be used to pass in serializable state that will be used during the Job processing.
         *
         * @param path example parameter passed in from the event
         */
        public ImmediateJob(String path) {
            // Maintain job state
            this.path = path;
        }

        /**
         * Run is the entry point for initiating the work to be done by this job.
         * The Sling job management mechanism will call run() to process the job.
         */
        public void run() {
            final Map<String, Object> authInfo = Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE,
                    (Object) "my-service-account");

            ResourceResolver resourceResolver = null;
            try {
                // Always use service users; never admin resource resolvers for "real" code
                resourceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo);

                // Access data passed into the Job from the Event
                Resource resource = resourceResolver.getResource(path);

                if (resource != null) {
                    ValueMap properties = resource.getValueMap();
                    // Do some work w this resource..
                }
            } catch (LoginException e) {
                log.error("Could not get service resolver", e);
            } finally {
                // Always close resource resolvers you open
                if (resourceResolver != null) {
                    resourceResolver.close();
                }
            }
        }
    }
}