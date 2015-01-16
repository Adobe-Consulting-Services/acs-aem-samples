package com.adobe.acs.samples.workflow.impl;

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
import java.util.Arrays;
import java.util.Collections;


@Component(
        label = "ACS-AEM-Samples - CQ Workflow Process",
        description = "ACS-AEM-Samples- Sample Workflow Process implementation",
        metatype = false,
        immediate = false,
        policy = ConfigurationPolicy.REQUIRE
)
@Properties({
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Sample Workflow Process implementation.",
                propertyPrivate = true
        ),
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "ActiveCQ",
                propertyPrivate = true
        ),
        @Property(
                label = "Workflow Label",
                name = "process.label",
                value = "Sample Workflow Process",
                description = "Label which will appear in the Adobe CQ Workflow interface"
        )
})
@Service
public class SampleProcessWorkflow implements WorkflowProcess {

    /**
     * OSGi Service References *
     */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /**
     * Fields *
     */

    private static final Logger log = LoggerFactory.getLogger(SampleProcessWorkflow.class);

    /**
     * Work flow execute method *
     */
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        // Get the Workflow data (the data that is being passed through for this work item)
        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if (!StringUtils.equals(type, "JCR_PATH")) {
            return;
        }
        // Get the path to the JCR resource from the payload
        final String path = workflowData.getPayload().toString();

        ResourceResolver resourceResolver = null;
        try {
            //get the resourceresolver from workflow session
            resourceResolver = getResourceResolver(workflowSession.getSession());
        } catch (LoginException e) {
           log.error("resolver could not be obtained",e);
        }
        //get the resource the payload points to
        Resource resource = resourceResolver.getResource(path);
        //do the things to the resource.

        // Standard Arguments metadata
        String argument = args.get("PROCESS_ARGS", "default value");
        // No parse "argument" as needed to extract delimited values

        // Custom WF inputs stored under ./metaData/argSingle and ./metadata/argMulti
        String singleValue = args.get("argSingle", "not set");
        String[] multiValue = args.get("argMulti", new String[]{"not set"});

        log.debug("Single Value: {}", singleValue);
        log.debug("Multi Value: {}", Arrays.toString(multiValue));

        // Save data for use in a subsequent Workflow step
        persistData(workItem, workflowSession, "set-for-next-workflow-step", "whatever data you want");

        throw new UnsupportedOperationException("Not supported yet.");
    }



    /**
     * Helper methods *
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
