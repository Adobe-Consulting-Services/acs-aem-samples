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

package com.adobe.acs.samples.services.impl;

import com.adobe.acs.samples.services.SampleMutableStateService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component(
        label = "ACS AEM Samples - Basic Service with Mutable Service state",
        description = "Sample implementation of a service with mutable state."
)
@Service
public class SampleMutableStateServiceImpl implements SampleMutableStateService {
    private static final Logger log = LoggerFactory.getLogger(SampleMutableStateServiceImpl.class);

    // "Normal" instance variables should be accessed via synchronized blocks
    private final Map<String, String> map = new HashMap<String, String>();

    // Use of a synchronized variable
    // Note: in this case single operations are synchronized, however if we were to iterate
    // over this list, we would need to place it in a sychronized block
    private final List<String> list = Collections.synchronizedList(new ArrayList<String>());

    // Atomic vars (available via the java.util.concurrent library) are thread-safe.
    // Prefer Atomic vars are they are safer and faster.
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    // Variables that are only modified during managed events in a OSGi Components lifecycle can be declared normally
    // These methods are activate, deactivate, modified, bind and unbind
    private String[] array = new String[]{};

    @Override
    public final void addToMap(final String key, final String val) {
        synchronized (map) {
            // Keep synchronized blocks as short as possible
            map.put(key, val);
        }
    }

    @Override
    public final String getFromMap(final String key) {
        synchronized (map) {
            // Keep synchronized blocks as short as possible
            return map.get(key);
        }
    }

    @Override
    public final void addToList(final String val) {
        list.add(val);
    }

    @Override
    public final int getListLength() {
        return list.size();
    }

    @Override
    public final void incremementCount() {
        atomicInteger.incrementAndGet();
    }

    @Override
    public final int getCount() {
        return atomicInteger.get();
    }

    @Activate
    public final void activate(Map<String, String> config) {
        // The only places the "array" class variable is modified is in the "activate" and "deactivate" methods.
        array = new String[2];
        array[0] = "hello";
        array[1] = "world";
    }

    @Deactivate
    public final void deactivate(Map<String, String> config) {
        // The only places the "array" class variable is modified is in the "activate" and "deactivate" methods.
        array = null;
    }
}