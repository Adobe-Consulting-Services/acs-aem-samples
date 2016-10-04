package com.adobe.acs.samples.migration;

import com.adobe.granite.jmx.annotation.Description;
import com.adobe.granite.jmx.annotation.Name;

import javax.management.MBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularDataSupport;

@Description("ACS AEM Samples - Sample Replication Harness MBean")
public interface SampleContentReplicationHarness {

    @Description("Activate the results of the query as synchronous and without versions")
    void activate(@Name(value="xpath, JCR-SQL, JCR-SQL2")String queryLanguage,
                  @Name(value="Query statement")String query);

    @Description("Set the Throttle and throttle Batch size")
    void setThrottle(@Name(value="Throttle in milliseconds")int throttle,
                     @Name(value="Throttle batch size")int batchSize);

    @Description("Aborts the replication. See the Replication Statistics for ID")
    void abort(@Name(value="ID")String id) throws MBeanException;


    /* MBean methods to return configuration, statistical and other general information about any running replication processes */

    @Description("Get replication statistics")
    TabularDataSupport getReplicationInfo() throws OpenDataException;

    @Description("Get throttle configuration")
    CompositeDataSupport getThrottleConfig() throws OpenDataException;
}
