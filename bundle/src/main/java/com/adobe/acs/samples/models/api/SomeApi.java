package com.adobe.acs.samples.models.api;

/**
 * Sling models can be used to implement interfaces of your fancy application logic...
 *
 * Usage: <code>resourceResolver.adaptTo(SomeApi.class)</code>
 * Implementation: <code>SomeApiModel</code>
 */
public interface SomeApi {

    /**
     * Does magic things...
     *
     * @return A magic number...
     */
    public int doMagic();

}
