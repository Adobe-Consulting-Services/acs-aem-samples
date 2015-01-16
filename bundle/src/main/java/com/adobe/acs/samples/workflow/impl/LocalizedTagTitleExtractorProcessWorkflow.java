package com.adobe.acs.samples.workflow.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component(
        label = "ACS-AEM-Samples  - Localized Tag Title Extractor Workflow",
        description = "ACS-AEM-Samples  Workflow Process implementation",
        metatype = false,
        immediate = false,
        policy = ConfigurationPolicy.REQUIRE
)
@Properties({
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Sample Workflow Process implementation - Writes tags titles to index-able property.",
                propertyPrivate = true
        ),
        @Property(
                label = "Vendor",
                name = Constants.SERVICE_VENDOR,
                value = "Adobe",
                propertyPrivate = true
        ),
        @Property(
                label = "Workflow Label",
                name = "process.label",
                value = "Localized Tag Title Extractor",
                description = "Writes localized tag Titles to a index-able Page/Asset property (tag-titles)."
        )
})
@Service
public class LocalizedTagTitleExtractorProcessWorkflow implements WorkflowProcess {

    public static final int MIN_TAG_DEPTH = 0;

    public static final String TYPE_CQ_PAGE_CONTENT = "cq:PageContent";
    public static final String TYPE_CQ_PAGE = "cq:Page";
    public static final String TYPE_DAM_ASSET = "dam:Asset";
    public static final String TYPE_DAM_ASSET_METADATA = "metatdata";
    public static final String REL_PATH_DAM_ASSET_METADATA = "jcr:content/metatdata";

    public static final String PATH_DELIMITER = "/";

    public static final String PROPERTY_TAG_TITLES = "tag-titles";
    public static final String PROPERTY_CQ_TAGS = "cq:tags";

    /**
     * OSGi Service References *
     */

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * Fields *
     */

    private static final Logger log = LoggerFactory.getLogger(LocalizedTagTitleExtractorProcessWorkflow.class);


    /**
     * Work flow execute method *
     */
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        boolean useUpstream = true;
        boolean useDownstream = true;

        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if (!StringUtils.equals(type, "JCR_PATH")) {
            return;
        }

        // Get the path to the JCR resource from the payload
        String path = workflowData.getPayload().toString();
        // Initialize some variables
        List<String> newTagTitles = new ArrayList<String>();
        List<String> newUpstreamTagTitles = new ArrayList<String>();
        List<String> newDownstreamTagTitles = new ArrayList<String>();
        Locale locale = null;

        ResourceResolver resourceResolver = null;

        try {
            resourceResolver = getResourceResolver(workflowSession.getSession());
            // Get the Resource representing the WF payload
            final Resource resource = resourceResolver.getResource(path);

            // Get the TagManager (using the same permission level as the Workflow's Session)
            final TagManager tagManager = resourceResolver.adaptTo(TagManager.class);

            // Use custom implementation to find the resource to look for cq:tags and write the
            // custom property "tag-titles" to
            final Resource contentResource = getContentResource(resource);
            if (contentResource == null) {
                log.error("Could not find a valid content resource node for payload: {}", resource.getPath());
                return;
            }
            // Gain access to the content resource's properties
            final ValueMap properties = contentResource.adaptTo(ValueMap.class);


            // Get the full tag paths (namespace:path/to/tag) from the content resource
            // This only works on the cq:tags property
            final Tag[] tags = tagManager.getTags(contentResource);

            // Get any previously applied Localized Tag Titles.
            // This is used to determine if changes if any updates are needed to this node.
            final String[] previousTagTitles = properties.get(PROPERTY_TAG_TITLES, new String[]{});

            if (!ArrayUtils.isEmpty(tags)) {
                // Derive the locale
                if (DamUtil.isAsset(resource)) {
                    // Dam assets use path segments to derive the locale (/content/dam/us/en/...)
                    locale = getLocaleFromPath(resource);
                } else {
                    // Page's use the jcr:language property accessed via the CQ Page API
                    Page page = resource.adaptTo(Page.class);
                    if (page != null) {
                        locale = page.getLanguage(true);
                    }
                }

                // Derive the Localized Tag Titles for all tags in the tag hierarchy from the Tags stored in the cq:tags property
                // This does not remove duplicate titles (different tag trees could repeat titles)
                if (useUpstream) {
                    newUpstreamTagTitles = tagsToUpstreamLocalizedTagTitles(tags, locale, tagManager);
                    newTagTitles.addAll(newUpstreamTagTitles);
                }

                if (useDownstream) {
                    newDownstreamTagTitles = tagsToDownstreamLocalizedTagTitles(tags, locale, tagManager, new ArrayList<String>(), 0);
                    newTagTitles.addAll(newDownstreamTagTitles);
                }

                if (!useUpstream && !useDownstream) {
                    newTagTitles.addAll(tagsToLocalizedTagTitles(tags, locale));
                }

            }

            try {
                // If the currently applied Tag Titles are the same as the derived Tag titles then skip!
                if (!isSame(newTagTitles.toArray(new String[]{}), previousTagTitles)) {
                    //Get the modifyable map of the payload resource
                    ModifiableValueMap props = contentResource.adaptTo(ModifiableValueMap.class);
                    // If changes have been made to the Tag Names, then apply to the tag-titles property
                    // on the content resource.
                    props.put(PROPERTY_TAG_TITLES, newUpstreamTagTitles.toArray(new String[newUpstreamTagTitles.size()]));
                    // Call commit to persist changes to the JCR
                    contentResource.getResourceResolver().commit();
                } else {
                    log.debug("No change in Tag Titles. Do not update this content resource.");
                }

            } catch (PersistenceException e) {
                log.error(e.getMessage());
            }
        } catch (LoginException e) {
            log.error(e.getMessage(),e);
        } finally {
            // Clean up after yourself please!!!
            if (resourceResolver != null) {
                resourceResolver.close();
                resourceResolver = null;
            }
        }
    }
    private ResourceResolver getResourceResolver(Session session) throws LoginException {

        return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap
                (JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                        session));

    }
    /**
     * Finds the proper "content" resource to read cq:tags from and write tag-titles to, based on
     * payload resource type.
     * <p/>
     * cq:Page
     * cq:PageContent
     * nt:unstructured acting as cq:PageContent
     * <p/>
     * dam:Asset
     * dam:Asset metadata
     *
     * @param payloadResource
     * @return
     */
    private Resource getContentResource(final Resource payloadResource) {

        if (isPrimaryType(payloadResource, TYPE_CQ_PAGE)) {

            /** cq:Page **/

            return payloadResource.getChild(JcrConstants.JCR_CONTENT);
        } else if (StringUtils.equals(payloadResource.getName(), JcrConstants.JCR_CONTENT) &&
                isPrimaryType(payloadResource, TYPE_CQ_PAGE_CONTENT)) {

            /** cq:PageContent **/

            return payloadResource;
        } else if (isPrimaryType(payloadResource, JcrConstants.NT_UNSTRUCTURED)) {

            /** nt:unstructured **/

            final Resource parent = payloadResource.getParent();

            if (parent != null &&
                    isPrimaryType(parent, TYPE_CQ_PAGE) &&
                    StringUtils.equals(payloadResource.getName(), JcrConstants.JCR_CONTENT)) {

                /** cq:Page / jcr:content(nt:unstructured) **/

                return payloadResource;
            } else if (StringUtils.equals(payloadResource.getName(), TYPE_DAM_ASSET_METADATA)) {
                if (parent != null && StringUtils.equals(parent.getName(), JcrConstants.JCR_CONTENT)) {
                    Resource grandParent = null;
                    if (parent != null) {
                        grandParent = parent.getParent();
                    }

                    if (grandParent != null && isPrimaryType(grandParent, TYPE_DAM_ASSET)) {
                        /** dam:Asset / jcr:content / metadata **/
                        return payloadResource;
                    }
                }
            }
        } else if (isPrimaryType(payloadResource, TYPE_DAM_ASSET)) {

            /** dam:Asset **/

            return payloadResource.getChild(REL_PATH_DAM_ASSET_METADATA);
        }

        /** Use the payload resource; Ex. a component resource that uses cq:tags **/

        return payloadResource;
    }
    /**
     * Checks if the jcr:PrimaryType of a resource matches the type param
     *
     * @param resource
     * @param type
     * @return
     */
    private boolean isPrimaryType(final Resource resource, final String type) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        String primaryType = properties.get(JcrConstants.JCR_PRIMARYTYPE, "__unknown__");
        return StringUtils.equals(type, primaryType);
    }
    /**
     * Derive the locale from the parent path segments (/content/us/en/..)
     *
     * @param resource
     * @return
     */
    private Locale getLocaleFromPath(final Resource resource) {
        final String[] segments = StringUtils.split(resource.getPath(), PATH_DELIMITER);

        String country = "";
        String language = "";

        for (final String segment : segments) {
            if (ArrayUtils.contains(Locale.getISOCountries(), segment)) {
                country = segment;
            } else if (ArrayUtils.contains(Locale.getISOLanguages(), segment)) {
                language = segment;
            }
        }

        if (StringUtils.isNotBlank(country) && StringUtils.isNotBlank(language)) {
            return LocaleUtils.toLocale(country + "-" + language);
        } else if (StringUtils.isNotBlank(country)) {
            return LocaleUtils.toLocale(country);
        } else if (StringUtils.isNotBlank(language)) {
            return LocaleUtils.toLocale(language);
        }

        return null;
    }
    /**
     * Returns localized Tag Titles for all the ancestor tags to the tags supplied in "tagPaths"
     * <p/>
     * Tags in
     *
     * @param tags
     * @param locale
     * @param tagManager
     * @return
     */
    private List<String> tagsToUpstreamLocalizedTagTitles(Tag[] tags, Locale locale, TagManager tagManager) {
        List<String> localizedTagTitles = new ArrayList<String>();

        for (final Tag tag : tags) {
            String tagID = tag.getTagID();

            boolean isLast = false;

            int count = StringUtils.countMatches(tagID, PATH_DELIMITER);
            while (count >= MIN_TAG_DEPTH && count >= 0) {

                final Tag ancestorTag = tagManager.resolve(tagID);
                if (ancestorTag != null) {
                    localizedTagTitles.add(getLocalizedTagTitle(ancestorTag, locale));
                }

                if (isLast) {
                    break;
                }

                tagID = StringUtils.substringBeforeLast(tagID, PATH_DELIMITER);
                count = StringUtils.countMatches(tagID, PATH_DELIMITER);

                if (count <= 0) {
                    isLast = true;
                }
            }
        }

        return localizedTagTitles;
    }

    /**
     * @param tag
     * @param locale
     * @return
     */
    private String getLocalizedTagTitle(Tag tag, Locale locale) {
        final String title = tag.getTitle();
        final String localizeTitle = tag.getTitle(locale);

        if (StringUtils.isNotBlank(localizeTitle)) {
            return localizeTitle;
        } else if (StringUtils.isNotBlank(title)) {
            return title;
        }

        return null;
    }
    /**
     * Returns localized Tag Titles for all the ancestor tags to the tags supplied in "tagPaths"
     * <p/>
     * Tags in
     *
     * @param tags
     * @param locale
     * @param tagManager
     * @param localizedTagTitles
     * @return
     */
    private List<String> tagsToDownstreamLocalizedTagTitles(final Tag[] tags, final Locale locale, final TagManager tagManager, List<String> localizedTagTitles, int depth) {
        depth++;

        for (final Tag tag : tags) {
            if (tag.listChildren() != null && tag.listChildren().hasNext()) {
                final List<Tag> children = IteratorUtils.toList(tag.listChildren());
                localizedTagTitles = tagsToDownstreamLocalizedTagTitles(children.toArray(new Tag[children.size()]), locale, tagManager, localizedTagTitles, depth);
            }

            if (depth > 1) {
                // Do not include the tags explicitly set on the resource
                localizedTagTitles.add(getLocalizedTagTitle(tag, locale));
            }
        }

        return localizedTagTitles;
    }

    /**
     * @param tags
     * @param locale
     * @return
     */
    private List<String> tagsToLocalizedTagTitles(final Tag[] tags, final Locale locale) {
        List<String> localizedTagTitles = new ArrayList<String>();

        for (final Tag tag : tags) {
            localizedTagTitles.add(getLocalizedTagTitle(tag, locale));
        }

        return localizedTagTitles;
    }

    /**
     * Checks if two String arrays are the same (same values and same order)
     *
     * @param a
     * @param b
     * @return
     */
    private boolean isSame(String[] a, String[] b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (!StringUtils.equals(a[i], b[i])) {
                return false;
            }
        }

        return true;
    }
}
