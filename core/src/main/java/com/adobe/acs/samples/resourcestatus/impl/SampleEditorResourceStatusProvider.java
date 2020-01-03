package com.adobe.acs.samples.resourcestatus.impl;

import com.adobe.granite.resourcestatus.ResourceStatus;
import com.adobe.granite.resourcestatus.ResourceStatusProvider;
import com.day.cq.wcm.commons.status.EditorResourceStatus;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This ResourceStatusProvider is comprised of 3 parts:
 *
 * 1. This class which defines the ResourceStatusProvider implmentation and returns a list of ResourceStatus for display
 * 2. The CompositeStatusType OSGi config that maps this ResourceStatusProviders type (sample-resource-status) to the AEM editor statusType (editor) so it will display on the Page and Experience Fragment editors.
 *    - /apps/acs-samples/config.author/com.adobe.granite.resourcestatus.impl.CompositeStatusType-sampleresourcestatus.xml
 * 3. The JavaScript/ClientLibrary that binds to the Actions added to the ResourceStatuses return from this service, and exposes behavior when the Actions are clicked on.
 *    - /apps/acs-samples/components/utilities/status/sample-resource-status/clientlibs
 */
@Component
public class SampleEditorResourceStatusProvider implements ResourceStatusProvider {
    private static final Logger log = LoggerFactory.getLogger(SampleEditorResourceStatusProvider.class);

    /**
     * This provider type value is mapped to the Status resource @statusTypes (editor, template-editor) via the
     */
    private static final String STATUS_PROVIDER_TYPE = "sample-resource-status";

    public String getType() {
        return STATUS_PROVIDER_TYPE;
    }

    public List<ResourceStatus> getStatuses(final Resource resource) {

        // Given the provided resource determine if this resource is subject to this Status
        // If it is, populate the statuses list with the appropriate ResourceStatuses
        // If not, return an empty array

        if (!accepts(resource)) {
            // This ResourceStatus provider cannot or has no status to provide for this resource.

            // Fail fast and early to prevent extra work from being done.
            return Collections.EMPTY_LIST;
        }

        // Do work to derive any required information to build the status.
        // Note that this will be invoked on ever resource registered to the getType() value, so ensure heavy operations are avoided to prevent over-taxing the system.

        final List<ResourceStatus> resourceStatuses = new LinkedList<ResourceStatus>();

        // Use the EditorResourceStatus builder to create status; this alleviated the pain of knowing the properties names that the OOTB statusbar component requires.
        EditorResourceStatus.Builder builder = new EditorResourceStatus.Builder(
                getType(),
                "My sample status",
                "A succinct but informative sample message");

        // SUCCESS, INFO, WARNING, ERROR
        builder.setVariant(EditorResourceStatus.Variant.SUCCESS);

        // Name of CoralUI icons: https://docs.adobe.com/docs/en/aem/6-0/develop/ref/coral-ui/docs/2.1.2-aem600-015/icon.html
        builder.setIcon("actions");

        // Default priorities: success -> 0, info -> 100000, warning -> 200000, error -> 300000
        // Higher the priority the more important
        builder.setPriority(15000);

        // Add actions
        // Action IDs will be used to attach behavior to IDs in the TouchUI via custom JS
        builder.addAction("do-something", "Do something");
        builder.addAction("do-something-else", "Do something else");

        // Use builder.addData(..) to attach properties the builder doe not directly support.
        // This is most often used if a custom status component overlay for the Editors is used.
        // How displaying the "shortMessage" (which displays when there are more than 1 available statuses) requires use of data.
        builder.addData("shortMessage", "Sample status");

        // Add this ResourceStatus to the list
        resourceStatuses.add(builder.build());

        // Return the ResourceStatues; typically 0 or 1 status are returned.
        return resourceStatuses;
    }

    /**
     * A method to check if the resource has a status to report by this provider.
     *
     * @param resource the resource the provider is acting upon
     * @return true if this resource should be processed, false if not.
     */
    private boolean accepts(Resource resource) {
        // Do some checks to see if this resource should have status reported on by this ResourceStatusProvider

        // Fail fast!
        return true;
    }
}