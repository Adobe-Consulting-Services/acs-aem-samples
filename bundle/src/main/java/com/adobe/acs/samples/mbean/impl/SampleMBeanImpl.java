/*
 * #%L
 * ACS AEM Samples
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.samples.mbean.impl;

import com.adobe.acs.samples.mbean.SampleMBean;
import com.adobe.granite.jmx.annotation.AnnotatedStandardMBean;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import javax.management.DynamicMBean;
import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component(
        label = "ACS AEM Samples - Sample MBean",
        description = "Example of exposing data via an MBean",
        immediate = true
)
@Properties({
        @Property(
                label = "MBean Name",
                name = "jmx.objectname",
                value = "com.adobe.acs.samples.mbean:type=SampleMBean",
                propertyPrivate = true
        )
})
@Service(value = DynamicMBean.class)
public final class SampleMBeanImpl extends AnnotatedStandardMBean implements SampleMBean {

    // Note: Since this is mutable state in a OSGi service is Concurrent/Atomic for thread safety
    private ConcurrentHashMap<Date, String> history;
    private AtomicReference<String> message;

    // MBeans must have a constructor that throws a NotCompliantMBeanException
    // and calls super(<the-mbean-class.class>);

    public SampleMBeanImpl() throws NotCompliantMBeanException {
        super(SampleMBean.class);

        // Other initialization can happen here
        history = new ConcurrentHashMap<Date, String>();
        message.set("uninitialized");
    }

    // Getters will invoke and display the value of the getter when the Mbean is requested
    // Note: Only methods exposed via the interface (SampleMBean) will be visible via JMX
    @Override
    // @Description is set at the interface level
    public final String getMessage() {
        return message.get();
    }

    @Override
    // @Description is set at the interface level
    public final void setMessage(String message) {
        this.message.set(message);
        this.history.put(new Date(), message);
    }

    @Override
    // @Description is set at the interface level
    // Other methods (non-getters) can be invoked via the MBean as long as they are exposed via the interface
    public final void clearHistory() {
        this.history.clear();
    }

    /**
     * This returns the message history of the Mbean in a Tabular format
     *
     * @return the message history as an Mbean Table
     * @throws OpenDataException
     */
    @Override
    // @Description is set at the interface level
    public final TabularDataSupport getHistory() throws OpenDataException {


        // Define the row for the MBean tabular data; This is comprised of:
        // * "Row Type": historicalMessage and Description: Historical Message
        // * Column Name: messageDate, message
        // * Column Descriptions: Message Date, Message
        // * Column Data Types: Date, String
        final CompositeType historicalMessageType = new CompositeType(
                "historicalMessage",
                "Historical Message",
                new String[]{"messageDate", "message" },
                new String[]{"Message Date", "Message" },
                new OpenType[]{SimpleType.DATE, SimpleType.STRING }
        );


        // Initialize the Table w our "Row Type" (historicalMessageType) via the new TabularDataSupport and new TabularType calls

        // http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/TabularDataSupport.html
        final TabularDataSupport tabularData = new TabularDataSupport(

                // http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/TabularType.html#TabularType(java.lang.String,%20java.lang.String,%20javax.management.openmbean.CompositeType,%20java.lang.String[])

                new TabularType("messageHistory",
                        "Message History",
                        historicalMessageType,
                        new String[]{"messageDate" } // These are the columns that uniquely index each row
                )
        );

        // Populate the MBean table
        for (final Map.Entry<Date, String> entry : this.history.entrySet()) {

            final Map<String, Object> data = new HashMap<String, Object>();
            // The keys in the map match to the 3rd param (columns names) in historicalMessageType defined above
            data.put("messageDate", entry.getKey());
            data.put("message", entry.getValue());

            // Add the row to the Mbean table
            tabularData.put(new CompositeDataSupport(historicalMessageType, data));
        }

        return tabularData;
    }
}
