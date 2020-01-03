package com.adobe.acs.samples.workflow.impl;

import com.adobe.acs.samples.SampleExecutor;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an example of programmatically starting an AEM Workflow.
 *
 * Prefer the Granite Workflow APIs over the CQ Apis, as the CQ APIs are old(er) and now wrap the Granite APIs.
 */
@Component(service = SampleExecutor.class)
public class StartWorkflowImpl implements SampleExecutor {
    private static final Logger log = LoggerFactory.getLogger(StartWorkflowImpl.class);

    public void startWorkflow(ResourceResolver resourceResolver) throws WorkflowException {
        final String payloadPath = "/content/dam/we-retail/en/activities/biking/cycling_1.jpg";

        // Get a workflow session from a resource resolver (the initiator of the workflow).
        final WorkflowSession workflowSession = resourceResolver.adaptTo(WorkflowSession.class);

        // Workflow Models changed location in AEM 6.4, and exist under /var
        final String aem64WorkflowModelPath = "/var/workflow/models/dam/update_asset";

        // Prior to AEM 6.4, workflow models lived under /etc/workflow/models
        final String aem63WorkflowModelPath = "/etc/workflow/models/dam/update_asset/jcr:content/model";

        // Get the Workflow Model object
        final WorkflowModel workflowModel = workflowSession.getModel(aem64WorkflowModelPath);

        // Create a workflow Data (or Payload) object pointing to a resource via JCR Path (alternatively, a JCR_UUID can be used)
        final WorkflowData workflowData = workflowSession.newWorkflowData("JCR_PATH", payloadPath);

        // Optionally pass in workflow metadata via a Map
        final Map<String, Object> workflowMetadata = new HashMap<>();
        workflowMetadata.put("anyData", "You Want");
        workflowMetadata.put("evenThingsLike", new Date());

        // Start the workflow!
        workflowSession.startWorkflow(workflowModel, workflowData, workflowMetadata);
    }

    @Override
    public String execute() {
        return null;
    }

    @Override
    public String execute(final ResourceResolver resourceResolver) {
        try {
            startWorkflow(resourceResolver);
            return "started the workflow!";
        } catch (WorkflowException e) {
            log.error("Unable to start the sample workflow", e);
            return "an error occurred!";
        }
    }
}

