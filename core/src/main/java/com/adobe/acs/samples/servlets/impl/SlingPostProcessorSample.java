package com.adobe.acs.samples.servlets.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.UUID;


/**
 * SlingPostProcessor's are called after a POST operation of the default SlingPostServlet but before changes are
 * persisted to the repository.
 *
 * @link https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#slingpostprocessor
 * @link https://sling.apache.org/apidocs/sling7/org/apache/sling/servlets/post/SlingPostProcessor.html
 */
@Component(
        service = SlingPostProcessor.class
)
public class SlingPostProcessorSample implements SlingPostProcessor {

    @Override
    public void process(SlingHttpServletRequest request, List<Modification> modifications) throws Exception {
        // First, check if our post processor implementation needs to do anything
        // Note: all registered SlingPostProcessor instances are called on all invocations of the Sling POST Servlet
        if (accepts(request)) {

            // Apply your custom changes: first, adapt the resource to a ModifiableValueMap
            // You need to get the resource from the ResourceResolver for the adaptTo to work.
            // The best way to get the path is from the Modifications list instead of request.getResource().getPath()
            final Resource resource = request.getResourceResolver().getResource( request.getResource().getPath() );
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);

            // Example: add an additional property to the resource
            String uuid = UUID.randomUUID().toString();
            properties.put("myapp.versionHash", uuid);

            // Record a "MODIFIED" entry in the modifications list
            modifications.add(Modification.onModified(resource.getPath()));
        }
    }

    protected boolean accepts(SlingHttpServletRequest request) {
        // If you want to modify only certain resources, you can check on the resource's "sling:resourceType"
        return "my-app/components/fancy-resource-type".equals(request.getResource().getResourceType());
    }

}
