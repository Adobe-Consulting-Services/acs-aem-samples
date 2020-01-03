package com.adobe.acs.samples.events.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.HashMap;
import java.util.Map;

@Component
public class SampleOsgiEventEmitter {

    @Reference
    private EventAdmin eventAdmin;

    public void execute() {
        // Event topics are main classification for an Event type.
        // Event topics typically (by convention) are prefixed to the Java package that generally pertains to the event.
        // This helps ensure customer events topics will be unique, and not accidentally engage w other provided Event Handlers,
        // and also helps developers understand where the code is that emits and handles the event.
        final String EVENT_TOPIC = "com/example/reports/GENERATE";

        // Create a map of data to pass as part of the event; These properties will be available (by Key) to any event consumers
        final Map<String, Object> eventProperties = new HashMap<String, Object>();
        eventProperties.put("name", "generatereport");
        // By convention, grouping sets of data logically is done with the "." notations (similar to OSGi Configuration Properties)
        // <logical-category>.<name>
        eventProperties.put("format.color", "red");
        eventProperties.put("format.size", "10");
        eventProperties.put("path" , "/some/path");

        // Create a new Event for the Topic and with the defined Properties
        final Event event = new Event(EVENT_TOPIC, eventProperties);

        /** Finally, emit the event so Event Handlers can listen for them an respond **/

        // EventAdmin.sendEvent() method sends the Event object SYNCHRONOUSLY
        eventAdmin.sendEvent(event);

        // EventAdmin.postEvent() method sends the Event object ASYNCHRONOUSLY
        eventAdmin.postEvent(event);
    }
}
