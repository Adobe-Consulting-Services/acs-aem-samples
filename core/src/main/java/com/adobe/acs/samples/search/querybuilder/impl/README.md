## Sample Facet Predicate Evaluator Client and Output

This code can be used to test out the SampleFacetPredicateEvaluator.

### Interface for OSGi Service

```
package com.adobe.acs.samples.querybuilder.facets;

import org.apache.sling.api.resource.ResourceResolver;
import javax.jcr.RepositoryException;

public interface SampleSearchClient {
    String search(ResourceResolver resourceResolver) throws RepositoryException;
}
```

### OSGi Service Implementation

```
package com.adobe.acs.samples.querybuilder.facets.impl;

import com.adobe.acs.samples.querybuilder.facets.SampleSearchClient;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.facets.Bucket;
import com.day.cq.search.facets.Facet;
import com.day.cq.search.result.SearchResult;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        label = "ACS AEM Samples - Sample Facet Predicate Evaluator Client"
)
@Service
public class SampleFacetPredicateClientImpl implements SampleSearchClient {
    private static final Logger log = LoggerFactory.getLogger(SampleFacetPredicateClientImpl.class);

    @Reference
    private QueryBuilder queryBuilder;

    public final String search(final ResourceResolver resourceResolver) throws RepositoryException {
        final Session session = resourceResolver.adaptTo(Session.class);

        final Map<String, String> map = new HashMap<String, String>();
        map.put("path", "/content/geometrixx");
        map.put("type", "cq:PageContent");
        map.put("1_facet.title", "Title");
        map.put("1_facet.value", "jcr:title");
        map.put("2_facet.title", "Description");
        map.put("2_facet.value", "jcr:description");


        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
        final SearchResult result = query.getResult();

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        pw.printf("Facet Count: %d", result.getFacets().size()).println();

        for(final Map.Entry<String, Facet> facet: result.getFacets().entrySet()) {
            pw.printf("Facet: %s", this.getFacetTitle(facet.getKey(), facet.getValue())).println();
            final List<Bucket> buckets = facet.getValue().getBuckets();

            for(final Bucket bucket : buckets) {
                pw.printf(" > Bucket: [ %s ] [ %d ]",
                        bucket.getValue(),
                        bucket.getCount()).println();
            }
            
            pw.println("--------------------------------------------------");
        }


        return sw.toString();
    }

    private String getFacetTitle(final String key, final Facet facet) {
        List<Bucket> buckets = facet.getBuckets();
        if(buckets.size() > 0) {
            Bucket bucket = buckets.get(0);
            String title = bucket.getPredicate().get("title");
            if (StringUtils.isNotBlank(title)) {
                return title;
            } 
        }
        return key;;
    }
}
```

## Sample Output

```
Facet Count: 3
Facet: Title
 > Bucket: [ PRESS CENTER ] [ 1 ]
 > Bucket: [ ACCOUNT REQUEST ] [ 1 ]
 > Bucket: [ GEOBLOG ] [ 1 ]
 > Bucket: [ GEOMETRIXX USER GROUP MEETING ] [ 1 ]
 > Bucket: [ NEWSLETTER ] [ 1 ]
 > Bucket: [ ENGLISH ] [ 1 ]
 > Bucket: [ ACCOUNT ] [ 1 ]
 > Bucket: [ NEWS ] [ 1 ]
 > Bucket: [ CHANGE PASSWORD ] [ 1 ]
 > Bucket: [ ARTICLES ] [ 1 ]
 > Bucket: [ PRESS RELEASES ] [ 1 ]
 > Bucket: [ TRIANGLE ] [ 1 ]
 > Bucket: [ PROFILES ] [ 1 ]
 > Bucket: [ DISCOVER GEOMETRIXX ] [ 1 ]
 > Bucket: [ COMPANY ] [ 1 ]
 > Bucket: [ DSC 2008 SHOW IN BERLIN ] [ 1 ]
 > Bucket: [ SUCCESS ] [ 1 ]
 > Bucket: [ OVERVIEW ] [ 4 ]
 > Bucket: [ PRODUCTS ] [ 1 ]
 > Bucket: [ PRESS CENTER ASSET VIEWER ] [ 1 ]
 > Bucket: [ MANAGEMENT TEAM ] [ 1 ]
 > Bucket: [ BOARD OF DIRECTORS ] [ 1 ]
 > Bucket: [ CUSTOMER SATISFACTION SURVEY ] [ 1 ]
 > Bucket: [ SUPPORT ] [ 1 ]
 > Bucket: [ REGISTER ] [ 1 ]
 > Bucket: [ EVENT EDIT FORM ] [ 1 ]
 > Bucket: [ MY GEOMETRIXX ] [ 1 ]
 > Bucket: [ SHAPECON LAS VEGAS ] [ 1 ]
 > Bucket: [ FEATURES ] [ 4 ]
 > Bucket: [ MANDELBROT SET ] [ 1 ]
 > Bucket: [ USER CONFERENCE ] [ 1 ]
 > Bucket: [ FORGOT PASSWORD ] [ 1 ]
 > Bucket: [ DSC BERLIN ] [ 1 ]
 > Bucket: [ EDIT PROFILE ] [ 1 ]
 > Bucket: [ SALES VOLUMES INCREASE ] [ 1 ]
 > Bucket: [ PARTNER & CUSTOMER TECHSUMMIT ] [ 1 ]
 > Bucket: [ EVENT VIEW FORM ] [ 1 ]
 > Bucket: [ TECHSUMMIT ] [ 1 ]
 > Bucket: [ EVENTS ] [ 1 ]
 > Bucket: [ SITEMAP ] [ 1 ]
 > Bucket: [ COMMUNITY ] [ 1 ]
 > Bucket: [ CIRCLE ] [ 1 ]
 > Bucket: [ ABOUT US ] [ 1 ]
 > Bucket: [ THANK YOU ] [ 4 ]
 > Bucket: [ TOOLBAR ] [ 1 ]
 > Bucket: [ CONTACT ] [ 1 ]
 > Bucket: [ SHAPECON 2009 IN VEGAS ] [ 1 ]
 > Bucket: [ PRESS CENTER ASSET EDITOR ] [ 1 ]
 > Bucket: [ SEARCH ] [ 2 ]
 > Bucket: [ LOGIN ] [ 1 ]
 > Bucket: [ SQUARE ] [ 1 ]
 > Bucket: [ VIEW PROFILE ] [ 1 ]
 > Bucket: [ FEEDBACK ] [ 1 ]
 --------------------------------------------------
Facet: type
 > Bucket: [ cq:PageContent ] [ 105 ]
 --------------------------------------------------
Facet: Description
 > Bucket: [ WHAT'S HAPPENING WITH GEOMETRIXX ] [ 1 ]
 > Bucket: [ EVENT DESCRIPTIONS ] [ 1 ]
```
