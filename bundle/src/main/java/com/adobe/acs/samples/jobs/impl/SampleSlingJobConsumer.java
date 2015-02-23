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

package com.adobe.acs.samples.jobs.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        label = "ACS AEM Samples - Sling Job Consumer",
        description = "Sample implementation of a custom Job Consumer",

        // One of the few cases where immediate = true; this is so the Event Listener starts listening immediately
        immediate = true
)
@Properties({
        @Property(
                label = "Job Topics",
                value = {"com/adobe/acs/samples/sample-job", "com/adobe/acs/samples/other-jobs/*"},
                description = "[Required] Job Topics this job consumer will to respond to.",
                name = JobConsumer.PROPERTY_TOPICS,
                propertyPrivate = true
        )
})
@Service
public class SampleSlingJobConsumer implements JobConsumer {
    private static final Logger log = LoggerFactory.getLogger(SampleSlingJobConsumer.class);

    @Override
    public JobResult process(final Job job) {

        // This is the Job's process method where the work will be

        // Jobs status is persisted in the JCR under /var/eventing so the management
        // of Jobs is NOT a wholly "in-memory" operations.

        // If you have guaranteed VERY FAST processing, it may be better to tie into an event

        // For information on all the data tied to the Job object
        // > http://sling.apache.org/apidocs/sling7/org/apache/sling/event/jobs/Job.html

        /**
         * A Custom property map can be passed in when issuing the job via the JobManager API
         *
         * Map<String, Object> map = new HashMap<String, Object>();
         * map.put("foo", "bar");
         *
         * jobManager.addJob("some/topic", map);
         */

        job.getProperty("foo");

        /**
         * Return the proper JobResult based on the work done...
         *
         * > OK : Processed successfully
         * > FAILED: Processed unsuccessfully and reschedule
         * > CANCEL: Processed unsuccessfully and do NOT reschedule
         * > ASYNC: Process through the JobConsumer.AsyncHandler interface
         */
        return JobConsumer.JobResult.OK;
    }
}
