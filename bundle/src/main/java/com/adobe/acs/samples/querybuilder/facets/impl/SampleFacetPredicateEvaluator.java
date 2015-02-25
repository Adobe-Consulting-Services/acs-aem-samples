package com.adobe.acs.samples.querybuilder.facets.impl;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.AbstractPredicateEvaluator;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.facets.FacetExtractor;
import com.day.cq.search.facets.extractors.DistinctValuesFacetExtractor;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.ArrayList;
import java.util.List;

@Component(
        label = "ACS AEM Samples - Sample QueryBuilder Predicate Evaluator",
        factory = "com.day.cq.search.eval.PredicateEvaluator/facet"
)
@Service

// Inherit from AbstractPredicateEvaluator so the basic override methods do not need to be implemented.
// AbstractPredicateEvaluator will not perform any inclusion or filtering by default.
// See: http://docs.adobe.com/docs/en/cq/current/javadoc/com/day/cq/search/eval/AbstractPredicateEvaluator.html

public class SampleFacetPredicateEvaluator extends AbstractPredicateEvaluator {
    private static final Logger log = LoggerFactory.getLogger(SampleFacetPredicateEvaluator.class);

    // NOTE: This Predicate does NOT perform ANY inclusion/filtering of results

    /*
        The Predicate name as defined by the @Component factory suffix (/facet) is "facet".

        The Predicate attributes (title and value) are totally custom, and be anything based on your use case.

        * This Predicate "title", is used only
        * This Predicate "value" is used to defined the property to build the bucket with. As seen below this
          is a purely custom implementation and the value could be used to get bucket values from anywhere.


        // QueryBuilder Predicate definition

        final Map<String, String> map = new HashMap<String, String>();
        map.put("type", "cq:PageContent");

        map.put("1_facet.title", "Title");
        map.put("1_facet.value", "jcr:title");

        map.put("2_facet.title", "Description");
        map.put("2_facet.value", "jcr:description");

    */

    public static final String TITLE = "title";
    public static final String VALUE = "value";

    @Override
    public FacetExtractor getFacetExtractor(final Predicate p, final EvaluationContext context) {

        try {
            // Sample Facet Extractor is a custom private Facet Extractor defined below.
            return new SampleFacetExtractor(p, context.getSession());
        } catch (RepositoryException e) {
            return null;
        }
    }

    // This extends DistinctValuesFacetExtractor
    private final class SampleFacetExtractor extends DistinctValuesFacetExtractor {
        private final Logger log = LoggerFactory.getLogger(SampleFacetExtractor.class);

        private final Session session;
        private final ValueFactory valueFactory;
        private final String valueProperty;

        // Pass in any context as the FacetExtractor is a POJO
        private SampleFacetExtractor(final Predicate p, final Session session) throws RepositoryException {
            // propertyRelPath: The property path; This is the value of p.get("value") for this custom Predicate; Should be unique
            // nodePathFilter: Can provide a filter to skip hit Nodes when extracting Facets
            // predicateTemplate: Usually p.clone();
            // valueParameterName: The Predicate parameter name that holds the value; This is less important here.
            super(p.get(VALUE),
                    null,
                    p.clone(),
                    VALUE);

            this.session = session;
            this.valueFactory = this.session.getValueFactory();
            this.valueProperty = p.get(VALUE);
        }

        // Helper method to transform a value to a Value as this is what the Buckets return
        private final Value createValue(String name) throws RepositoryException {
            return this.valueFactory.createValue(name);
        }

        @Override
        protected String getBucketValue(String value) {
            // Used to format or transform the bucket value to Display (Bucket.getValue())
            return StringUtils.upperCase(value);
        }

        @Override
        protected List getValues(Node hit) throws RepositoryException {
            // This method is called for every Hit result
            // This method is used to create a list of Values that will then automatically be split into various Buckets

            final List<Value> values = new ArrayList<Value>();

            // Note: This is an unusual case where JCR API is preferred over Sling Resource APIs
            // as performance is PARAMOUNT in FacetExtractors and the Node API
            // will be faster. 

            // Perform some logic here to figure out
            // 1) If you want to put this hit into a bucket
            // 2) One hit can inform as many buckets of this facet as you want

            if (StringUtils.startsWith(hit.getPath(), "/content/geometrixx/en")) {

                // Get the value from the predicate value; This could be anything, 
                // but usually makes sense that the Predicate definition helpers drive this.
                if (hit.hasProperty(this.valueProperty)) {
                    final String value = hit.getProperty(this.valueProperty).toString();
                    
                    if (StringUtils.isNotBlank(value)) {
                        // Add any Value; like values will be automatically grouped in the same Bucket
                        values.add(this.createValue(value));
                    }
                }
            }

            // Filter out any values using DistinctValuesFacetExtractor.filter() which
            // filters for distinct values.
            return filter(values, this.valueFactory);
        }
    }
}
