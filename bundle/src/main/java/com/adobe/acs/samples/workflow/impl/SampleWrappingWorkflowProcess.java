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

package com.adobe.acs.samples.workflow.impl;

import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Collections;


@Component(
        label = "ACS AEM Samples - AEM Wrapping Workflow Process",
        description = "ACS AEM Samples - Sample Wrapping Workflow Process implementation"
)
@Properties({
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Sample Wrapping Workflow Process implementation.",
                propertyPrivate = true
        ),
        @Property(
                label = "Workflow Label",
                name = "process.label",
                value = "Sample Wrapping Workflow Process",
                description = "Label which will appear in the AEM Workflow interface; This should be unique across "
                        + "Workflow Processes",
                propertyPrivate = true
        )
})
@Service
/**
 * If the Workflow Process you are wrapping is a CQ WF Process, then this wrapping process must also be a CQ Workflow Process.
 * If the Workflow Process you are wrapping is a Granite WF Process, then this wrapping process must also be a Granite Workflow Process.
 */
public class SampleWrappingWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(SampleCQWorkflowProcess.class);

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /**
     * This gets the Workflow Process to wrap using `target =` option.
     *
     * If the wrapped Workflow Process needed to be specified via PROCESS_ARGS, the approach used in
     * `com.adobe.acs.samples.services.impl.SampleMultiReferenceServiceImpl` can be used to collect all Workflow Process
     * references into a ConcurrentHaspMap and select the service reference object and retrieved via the class name,
     * which can be passed in via PROCESS_ARGs.
     */
    @Reference(target = "serivce.pid=com.day.cq.wcm.workflow.process.CreateVersionProcess")
    WorkflowProcess wrappedWorkflowProcess;

    /**
     * The method called by the AEM Workflow Engine to perform Workflow work.
     *
     * @param workItem the work item representing the resource moving through the Workflow
     * @param workflowSession the workflow session
     * @param args arguments for this Workflow Process defined on the Workflow Model (PROCESS_ARGS, argSingle, argMulti)
     * @throws WorkflowException when the Workflow Process step cannot complete. This will cause the WF to retry.
     */
    @Override
    public final void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws
            WorkflowException {

        /* Get the Workflow Payload */

        // Get the Workflow data (the data that is being passed through for this work item)
        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR; The other (less common) type is JCR_UUID
        if (!StringUtils.equals(type, "JCR_PATH")) {
            return;
        }
        // Get the path to the JCR resource from the payload
        final String path = workflowData.getPayload().toString();

        /* Do work on the Payload; Remember to use Sling APIs as much as possible */

        ResourceResolver resourceResolver = null;
        try {
            // Get the ResourceResolver from workflow session
            resourceResolver = getResourceResolver(workflowSession.getSession());


            /* This is where the wrapping takes place; always check to make sure the wrapped Workflow Process reference has not gone away */

            if (wrappedWorkflowProcess == null ) {
                // The wrapped workflow process is null! Handle based on your requirements.
                log.error("Wrapped Workflow Process is null");
            } else if (isProcessableWithWrappedWorkflow(resourceResolver, path)) {
                // Check ig the payload is a candidate for executing through the wrapped workflow; this can contain custom logic to make this determination.
                wrappedWorkflowProcess.execute(workItem, workflowSession, args);
            } else {
                // If not processable, its always good to log as DEBUG to INFO process w Wrapped Process for visibility
                log.info("Skipping processing [ {} ] with wrapped workflow process [ {} ]", path, wrappedWorkflowProcess.getClass().getName());

            }
        } catch (Exception e) {
            // If an error occurs that prevents the Workflow from completing/continuing - Throw a WorkflowException
            // and the WF engine will retry the Workflow later (based on the AEM Workflow Engine configuration).

            log.error("Unable to complete processing the Workflow Process step", e);

            throw new WorkflowException("Unable to complete processing the Workflow Process step", e);
        }
    }

    /**
     * Encapsulate your logic for determining if the payload should be processed by the wrapped workflow process.
     * In this sample, we ONLY want to call the OOTB Create Versions workflow on non-SubAssets.
     *
     * @param resourceResolver the workflow sessions' resource resolver
     * @param payloadPath the path to the payload
     * @return true if the wrapped workflow processs should bec alled.
     */
    private boolean isProcessableWithWrappedWorkflow(ResourceResolver resourceResolver, String payloadPath) {
        Resource resource = resourceResolver.getResource(payloadPath);

        // Only process NON-Sub-assets with the wrapped workflow
        return !DamUtil.isSubAsset(resource);
    }

    /**
     * Helper methods.
     */
    private ResourceResolver getResourceResolver(Session session) throws LoginException {
        return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                session));
    }

}