package com.adobe.acs.samples;

// Simple, generic Interface for testing out Impl services
// The use of this should be filtered via the OSGi Service's PID
public interface SampleExecutor {
    
    String execute();
    
}
