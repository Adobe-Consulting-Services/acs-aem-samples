package com.adobe.acs.samples.migration.impl;


import com.adobe.acs.samples.migration.SampleContentReplicationHarness;
import com.adobe.granite.jmx.annotation.AnnotatedStandardMBean;
import com.adobe.granite.jmx.annotation.Name;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component(
        label = "ACS AEM Samples - Sample Content Replication Harness",
        description = "Example of creating custom tooling to replicate content in a performant and controlled manner.",
        immediate = true
)
@Properties({
        @Property(
                label = "MBean Name",
                name = "jmx.objectname",
                value = "com.adobe.acs.samples.migration:type=Sample Content Replication Harness",
                propertyPrivate = true
        )
})
@Service(value = DynamicMBean.class)
public final class SampleContentReplicationHarnessImpl extends AnnotatedStandardMBean implements SampleContentReplicationHarness {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Throttle and Batch size are global to all replication executions by this Mbean, but can be reset at any time via setThrottle(..)
    private AtomicInteger throttle = new AtomicInteger(0);
    private AtomicInteger batchSize = new AtomicInteger(1000);

    // Track replication requests so that they are reportable by the MBean (see getReplicationInfo() and getThrottleConfig() below)
    private ConcurrentHashMap<String, ReplicationInfo> replicationInfo = new ConcurrentHashMap<String, ReplicationInfo>();

    @Reference
    private Replicator replicator;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    public SampleContentReplicationHarnessImpl() throws NotCompliantMBeanException {
        super(SampleContentReplicationHarness.class);
    }

    public <T> SampleContentReplicationHarnessImpl(T implementation, Class<T> mbeanInterface) throws NotCompliantMBeanException {
        super(implementation, mbeanInterface);
    }

    @Override
    public void activate(String queryLanguage, String queryStatement) {
        // Create leanest replication options for activation
        ReplicationOptions options = new ReplicationOptions();
        // Do not create new versions as this adds to overhead
        options.setSuppressVersions(true);
        // Avoid sling job overhead by forcing synchronous. Note this will result in serial activation.
        options.setSynchronous(true);
        // Do NOT suppress status update of resource (set replication properties accordingly)
        options.setSuppressStatusUpdate(false);

        // Track replication activity for reporting via the MBean get methods
        final ReplicationInfo replicationInfo = new ReplicationInfo(queryLanguage, queryStatement, ReplicationActionType.ACTIVATE);
        this.replicationInfo.put(replicationInfo.getId(), replicationInfo);

        this.replicate(queryLanguage, queryStatement, ReplicationActionType.ACTIVATE, options, replicationInfo);
    }

    @Override
    public void setThrottle(int throttle, int batchSize) {
        // Provide a way to adjust the throttle and batch size while the replication is running
        this.throttle.set(throttle);
        this.batchSize.set(batchSize);
    }

    @Override
    public void abort(@Name(value = "ID") String id) throws MBeanException {
        ReplicationInfo replicationInfo = this.replicationInfo.get(id);
        if (replicationInfo == null) {
            throw new MBeanException(new IllegalArgumentException("Could not locate replication with ID [ " + id + " ] so nothing to abort."));
        }

        if (!Status.COMPLETE.equals(replicationInfo.getStatus())) {
            replicationInfo.setStatus(Status.ABORT);
        }
    }

    private void replicate(String queryLanguage, String query, ReplicationActionType type, ReplicationOptions options, ReplicationInfo replicationInfo) {
        // As an MBean, use the resource resolver (security context) of the thread that initiated the MBean call.
        final ResourceResolver resourceResolver = resourceResolverFactory.getThreadResourceResolver();
        final Session session = resourceResolver.adaptTo(Session.class);

        final Iterator<Resource> results = resourceResolver.findResources(query, queryLanguage);
        while (results.hasNext()) {
            // Always time your operations
            long start = System.currentTimeMillis();

            // Always add a kill-switch to long running, potentially expensive processes.
            // This can be triggered via the MBean's abort(..) method.
            if (Status.ABORT.equals(replicationInfo.getStatus())) {
                log.info("Replication [ {} ] killed. Aborting immediately.", replicationInfo.getId());
            }

            final Resource resource = results.next();

            // Replicate the resource
            try {
                // Handle successes
                replicator.replicate(session, type, resource.getPath(), options);
                replicationInfo.incrementSuccess();
                log.info("Replicated resource [ {} ] in [ {} ms ]", resource.getPath(), System.currentTimeMillis() - start);
            } catch (ReplicationException e) {
                // Handle failures
                replicationInfo.incrementError();
                log.error("Could NOT replicate resource [ {} ]", resource.getPath(), e);
            }

            // Always support throttling to ensure replication activities can be lessened if they begin to adversely effect the system.
            if (throttle.get() > 0
                    && batchSize.get() > 0
                    && replicationInfo.getTotal() % batchSize.get() == 0) {
                try {
                    log.info("Sleeping replication for [ {} ms }", throttle.get());
                    // When introducing longer/frequent throttle times, clearly indicate that throttle/pause is occurring to prevent confusing this intended pause with system overload.
                    replicationInfo.setStatus(Status.SLEEPING);
                    Thread.sleep(throttle.get());
                    replicationInfo.setStatus(Status.RUNNING);
                } catch (InterruptedException e) {
                    log.error("Could not throttle replication via Thread.sleep(..). Moving ahead without throttling. Abort this replication if needed.", e);
                }
            }
        }
    }

    @Override
    // MBean method
    // @Description is set at the interface level
    public final CompositeDataSupport getThrottleConfig() throws OpenDataException {

        final CompositeType throttleConfigurationType = new CompositeType(
                "Throttle Configuration",
                "Throttle Configuration",
                new String[]{"Throttle (in ms)", "Batch size"},
                new String[]{"Throttle (in ms)", "Batch size"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.INTEGER}
        );

        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("Throttle (in ms)", this.throttle.get());
        data.put("Batch size", this.batchSize.get());
        return new CompositeDataSupport(throttleConfigurationType, data);
    }


    @Override
    // MBean method
    // @Description is set at the interface level
    public final TabularDataSupport getReplicationInfo() throws OpenDataException {
        // Define the row for the MBean tabular data; This is comprised of:
        // * "Row Type": Replication Statistics and Description: Replication Statistics
        // * Column Name: ID, Status, Start Date, Replication Type, Query Language, Query Statement, Running Count, Time Elapsed (in seconds)
        // * Column Descriptions: ID, Start Date, Replication Type, Query Language, Query Statement, Running Count, Time Elapsed (in seconds), "Complete"
        // * Column Data Types: String, String, Date, String, String, String, Integer, Long
        final CompositeType replicationInfoType = new CompositeType(
                "Replication Information",
                "Replication Information",
                new String[]{"Status", "ID", "Started At", "Replication Action", "Query Language", "Query Statement", "Success Count", "Error Count", "Total Count", "Time Elapsed (in seconds)"},
                new String[]{"Status", "ID", "Started At", "Replication Action", "Query Language", "Query Statement", "Success Count", "Error Count", "Total Count", "Time Elapsed (in seconds)"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.DATE, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.LONG}
        );


        // Initialize the Table w our "Row Type" (Replication ReplicationInfo) via the new TabularDataSupport and new TabularType calls

        // http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/TabularDataSupport.html
        final TabularDataSupport tabularData = new TabularDataSupport(
                // http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/TabularType.html#TabularType(java.lang.String,%20java.lang.String,%20javax.management.openmbean.CompositeType,%20java.lang.String[])
                new TabularType("Replication Info",
                        "Replication Info",
                        replicationInfoType,
                        new String[]{"ID"} // These are the columns that uniquely index each row
                )
        );

        // Populate the MBean table
        for (final Map.Entry<String, ReplicationInfo> entry : this.replicationInfo.entrySet()) {
            final ReplicationInfo replicationInfo = entry.getValue();
            final Map<String, Object> data = new HashMap<String, Object>();
            // The keys in the map match to the 3rd param (columns names) in historicalMessageType defined above
            data.put("Status", replicationInfo.getStatus());
            data.put("ID", replicationInfo.getId());
            data.put("Start Time", replicationInfo.getStartedAt().getTime());
            data.put("Replication Action", replicationInfo.getReplicationActionType());
            data.put("Query Language", replicationInfo.getQueryLanguage());
            data.put("Query Statement", replicationInfo.getQueryStatement());
            data.put("Success Count", replicationInfo.getSuccess());
            data.put("Error Count", replicationInfo.getError());
            data.put("Total Count", replicationInfo.getTotal());
            data.put("Time Elapsed (in seconds)", replicationInfo.getTimeElapsedInSeconds());

            // Add the row to the Mbean table
            tabularData.put(new CompositeDataSupport(replicationInfoType, data));
        }

        return tabularData;
    }

    /**
     * RequestInfo Statuses
     **/
    enum Status {
        RUNNING,
        ABORT,
        SLEEPING,
        COMPLETE
    }

    /**
     * This is a class that holds each replication actions running info/replicationInfo for reporting via the MBean.
     * <p>
     * Exposing this data via the MBean provides immediate clarity into the replication operations that have been requested to run.
     */
    private class ReplicationInfo {
        private final String id;
        private final Calendar startedAt;
        private final String queryLanguage;
        private final String queryStatement;
        private final ReplicationActionType replicationActionType;
        private final AtomicInteger success = new AtomicInteger(0);
        private final AtomicInteger error = new AtomicInteger(0);

        private Status status;

        public ReplicationInfo(String queryLanguage, String queryStatement, ReplicationActionType replicationActionType) {
            this.id = UUID.randomUUID().toString();
            this.startedAt = Calendar.getInstance();
            this.queryLanguage = queryLanguage;
            this.queryStatement = queryStatement;
            this.replicationActionType = replicationActionType;
            this.setStatus(Status.RUNNING);
        }

        public String getId() {
            return id;
        }

        public void incrementError() {
            this.error.incrementAndGet();
        }

        public int getError() {
            return this.error.get();
        }

        public Calendar getStartedAt() {
            return startedAt;
        }

        public String getQueryLanguage() {
            return queryLanguage;
        }

        public String getQueryStatement() {
            return queryStatement;
        }

        public ReplicationActionType getReplicationActionType() {
            return replicationActionType;
        }

        public int getSuccess() {
            return success.get();
        }

        public long getTimeElapsedInSeconds() {
            return (System.currentTimeMillis() - this.getStartedAt().getTimeInMillis()) / 1000;
        }

        public void incrementSuccess() {
            this.success.incrementAndGet();
        }

        public Status getStatus() {
            synchronized (status) {
                return status;
            }
        }

        public void setStatus(Status status) {
            synchronized (status) {
                this.status = status;
            }
        }

        public int getTotal() {
            return this.getError() + this.getSuccess();
        }
    }

}