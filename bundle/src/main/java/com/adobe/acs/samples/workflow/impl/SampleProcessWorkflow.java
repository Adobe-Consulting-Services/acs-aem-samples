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

package com.adobe.acs.samples.workflow.impl;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.Collections;


@Component(
        label = "ACS AEM Samples - AEM Workflow Process",
        description = "ACS AEM Samples - Sample Workflow Process implementation"
)
@Properties({
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Sample Workflow Process implementation.",
                propertyPrivate = true
        ),
        @Property(
                label = "Workflow Label",
                name = "process.label",
                value = "Sample Workflow Process",
                description = "Label which will appear in the AEM Workflow interface; This should be unique across "
                        + "Workflow Processes",
                propertyPrivate = true
        )
})
@Service
public class SampleProcessWorkflow implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(SampleProcessWorkflow.class);

    @Reference
    ResourceResolverFactory resourceResolverFactory;

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


        /* Get Workflow Process Arguments */

        // These args are specified on the Workflow Model using this WF Process.


        /* Process Args */
        // These are free-form textfields; the values of these may need to be parsed based on
        // expected in put formats

        String processArgs = args.get("PROCESS_ARGS", "default value");
        String[] proccesArgsVals = StringUtils.split(processArgs, ",");


        /* Single and MultiValue args */

        // Some WF Process steps support Single and MultiValue args; these are can access via named properties
        // Custom WF inputs stored under ./metaData/argSingle and ./metadata/argMulti
        String singleValue = args.get("argSingle", "not set");
        String[] multiValue = args.get("argMulti", new String[]{"not set"});

        log.debug("Single Value: {}", singleValue);
        log.debug("Multi Value: {}", Arrays.toString(multiValue));


        /* Get data set in prior Workflow Steps */
        String previouslySetData = this.getPersistedData(workItem, "set-in-previous-workflow-step", String.class);


        /* Do work on the Payload; Remember to use Sling APIs as much as possible */

        ResourceResolver resourceResolver = null;
        try {
            // Get the ResourceResolver from workflow session
            resourceResolver = getResourceResolver(workflowSession.getSession());

            // Get the resource the payload points to; Keep in mind the payload can be any resource including
            // a AEM WF Package which must be processes specially.
            Resource resource = resourceResolver.getResource(path);


            // Do work ....


            // Save data for use in a subsequent Workflow step
            persistData(workItem, workflowSession, "set-for-next-workflow-step", "whatever data you want");

        } catch (Exception e) {
            // If an error occurs that prevents the Workflow from completing/continuing - Throw a WorkflowException
            // and the WF engine will retry the Workflow later (based on the AEM Workflow Engine configuration).

            log.error("Unable to complete processing the Workflow Process step", e);

            throw new WorkflowException("Unable to complete processing the Workflow Process step", e);
        }
    }


    /**
     * Helper methods.
     */

    private <T> boolean persistData(WorkItem workItem, WorkflowSession workflowSession, String key, T val) {
        WorkflowData data = workItem.getWorkflow().getWorkflowData();
        if (data.getMetaDataMap() == null) {
            return false;
        }

        data.getMetaDataMap().put(key, val);
        workflowSession.updateWorkflowData(workItem.getWorkflow(), data);

        return true;
    }

    private <T> T getPersistedData(WorkItem workItem, String key, Class<T> type) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, type);
    }

    private <T> T getPersistedData(WorkItem workItem, String key, T defaultValue) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, defaultValue);
    }

    private ResourceResolver getResourceResolver(Session session) throws LoginException {
            return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                    session));
    }
}
