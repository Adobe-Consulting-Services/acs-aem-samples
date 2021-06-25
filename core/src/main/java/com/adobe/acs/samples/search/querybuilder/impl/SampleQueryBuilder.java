package com.adobe.acs.samples.search.querybuilder.impl;

import com.adobe.acs.samples.services.SampleService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query Builder is a power and useful interface for creating and executing queries in AEM.
 *
 * QueryBuilder should be preferred over JCR_SQL2 or XPath for the following reasons:
 * - QueryBuilder is able to execute queries that cannot fully execute via the Oak Query Engine by executing predicates (as needed) post query via results filtering
 * - Custom QueryBuilder predicates can be created and re-used across your application, consolidating query business logic
 * - QueryBuilder is maintained by Adobe so may be enhanced (as needed) to align to the AEM Platform (aka Oak) for increased performance
 * - Provides high-level abstraction that can be leveraged via Java Code or via its HTTP API (HTTP API for author-side activities)
 * - Removes need to manually construct XPath or JCR_SQL2 queries by hand (concatenation) which if coded improperly are susceptible to injection attacks.
 *
 * Also see Adobe Docs: https://docs.adobe.com/docs/en/aem/6-2/develop/search/querybuilder-api.html
 */

// Illustrates how to use the Query Builder API for querying AEM with a number of the supported options

@Component(
        service = SampleService.class
)
public class SampleQueryBuilder implements SampleService {
    private static final Logger log = LoggerFactory.getLogger(SampleQueryBuilder.class);

    // ISGO8601 Date format is used for the Date Predicates
    final static String ISO8601_DATE = ("YYYY-MM-DD'T'HH:mm:ss.SSSZ");

    // Get the QueryBuilder OSGi Service
    // Can also be obtained via `resourceResolver.adaptTo(QueryBuilder.class)`
    @Reference
    private QueryBuilder queryBuilder;

    // The user's security context (aka ResourceResolver or JCR Session) must be provided to the QueryBuilder to perform the query in the call
    public void doWork(ResourceResolver resourceResolver) throws PersistenceException {
        final SimpleDateFormat iso8601 = new SimpleDateFormat(ISO8601_DATE);

        final String fulltext = "Some word or phrase";

        final Map<String, String> map = new HashMap<String, String>();

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/TypePredicateEvaluator.html
        map.put("type", "cq:Page");

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/PathPredicateEvaluator.html
        map.put("path", "/content");
        map.put("path.exact", "true"); // defaults to true
        map.put("path.flat", "true");
        map.put("path.self", "true");

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/FulltextPredicateEvaluator.html
        map.put("fulltext", fulltext);
        map.put("fulltext.relPath", "jcr:content"); // Optional


        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/JcrBoolPropertyPredicateEvaluator.html
        map.put("boolproperty", "jcr:content/hideInNav");
        map.put("boolproperty.value", "false");

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/JcrPropertyPredicateEvaluator.html
        map.put("property", "jcr:content/jcr:title");
        map.put("property.depth", "2"); // defaults to 0
        map.put("property.value", "the value to match");
        map.put("property.1_value", "another value to match");
        map.put("property.and", "true"); // defaults to false
        map.put("property.operation", "equals"); // equals (default), unequals, like, not, exists

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/DateRangePredicateEvaluator.html
        map.put("daterange.property", "jcr:content/cq:lastModified"); // or use jcr:content/jcr:lastModified
        map.put("daterange.lowerBound", iso8601.format(new Date(10000)));
        map.put("daterange.lowerOperation", ">"); // Or >=
        map.put("daterange.upperBound", iso8601.format(new Date(20000)));
        map.put("daterange.upperOperation", "<"); // Or <=
        map.put("daterange.timeZone", "-5");


        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/DateRangePredicateEvaluator.html
        map.put("rangeproperty.property", "jcr:content/score");
        map.put("rangeproperty.lowerBound", "10.0001");
        map.put("rangeproperty.lowerOperation", ">"); // Or >=
        map.put("rangeproperty.upperBound", "99");
        map.put("rangeproperty.upperOperation", "<"); // Or <=
        map.put("rangeproperty.decimal", "true");


        //https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/RelativeDateRangePredicateEvaluator.html
        //  supports the bugzilla syntax 1s 2m 3h 4d 5w 6M 7y, else in milliseconds
        map.put("relativedaterange.property", "jcr:content/cq:lastModified"); // or use jcr:content/jcr:lastModified
        map.put("relativedaterange.lowerBound", "60000");
        map.put("relativedaterange.lowerOperation", ">"); // Or >=
        map.put("relativedaterange.upperBound", "4h 30m 10s");
        map.put("relativedaterange.upperOperation", "<"); // Or <=


        // Tag Predicate - this has been moved into an impl package so Javadocs are not genereated for it.
        map.put("tagid.property", "jcr:content/cq:tags"); // Default is 'cq:tags'
        map.put("tagid.1_value", "namespace-a:some/tag"); // Note the #_ prefix for values is required.
        map.put("tagid.2_value", "namespace-a:some/other/tag");
        map.put("tagid.3_value", "namespace-z:something/else");
        map.put("tagid.and", "true"); // "Ands" the tag value matches; default is false

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/SimilarityPredicateEvaluator.html
        map.put("similar", "/content/site/products/product-x");
        map.put("similar.local", ".");

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/NodenamePredicateEvaluator.html
        map.put("nodename", "some-node-name");

        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/eval/PermissionPredicateEvaluator.html
        map.put("hasPermission", "crx:replicate,jcr:write,jcr:removeNode");


        // Predicates (prefixed by 'p.')
        // https://docs.adobe.com/content/docs/en/aem/6-2/develop/ref/javadoc/com/day/cq/search/Predicate.html

        // Excerpt
        // Set excerpt to true to collect the excerpts; But be careful as this requires the Hit result object to collect the excerpt from.
        map.put("p.excerpt", "true");

        // Order By
        map.put("orderby", "@jcr:content/jcr:lastModified");
        map.put("orderby.sort", "desc");
        map.put("orderby.case", "ignore");

        // Offsets and Limits; usually used for pagination
        map.put("p.offset", "0");
        map.put("p.limit", "20");

        // Always set guessTotal to true unless you KNOW your result set will be small and counting it will be fast!
        map.put("p.guessTotal", "true");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));

        SearchResult result = query.getResult();

        // QueryBuilder has a leaking ResourceResolver, so the following work around is required.
        ResourceResolver leakingResourceResolver = null;

        try {
            // Iterate over the Hits if you need special information
            for (final Hit hit : result.getHits()) {
                if (leakingResourceResolver == null) {
                   // Get a reference to QB's leaking ResourceResolver
                   leakingResourceResolver = hit.getResource().getResourceResolver();
                }
                // Returns the path of the hit result
                String path = hit.getPath();

                // Always get your resources that you might pass out of this method by resolving w/ the ResourceResolver YOU provide (and not the leaking one in QB as we will close the leaking resolver in the finally block)    
                Resource resource = resourceResolver.getResource(hit.getPath());
                ValueMap properties = resource.getValueMap();

                // Requires setting query.setExcerpt(true) prior to query execution
                String excerpt = hit.getExcerpt();
                Map<String, String> excerpts = hit.getExcerpts();
            }

            // A common use case it to collect all the resources that represent hits and put them in a list for work outside of the search service
            final List<Resource> resources = new ArrayList<Resource>();
            for (final Hit hit : result.getHits()) {
                if (leakingResourceResolver == null) {
                   // Get a reference to QB's leaking ResourceResolver
                   leakingResourceResolver = hit.getResource().getResourceResolver();
                }
                resources.add(resourceResolver.getResource(hit.getPath()));
            }

        } catch (RepositoryException e) {
            log.error("Error collecting search results", e);
        } finally {
            if (leakingResourceResolver != null) {
                // Always Close the leaking QueryBuilder resourceResolver.
                leakingResourceResolver.close();    
            }        
        }        
    }

    public String helloWorld() {
        return "Hello from " + this.getClass().getName();
    }
}
