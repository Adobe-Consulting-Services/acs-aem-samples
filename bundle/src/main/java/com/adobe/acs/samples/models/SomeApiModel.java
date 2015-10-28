package com.adobe.acs.samples.models;

import com.adobe.acs.samples.models.api.SomeApi;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Inject;

@Model(adaptables = ResourceResolver.class, adapters = SomeApi.class)
public class SomeApiModel implements SomeApi {

    @Inject
    @Self
    protected ResourceResolver resolver;

    /** Simply implement the method of your interface */
    @Override
    public int doMagic() {
        // Do fancy stuff...
        return resolver.getResource("/var/magic-resource").getValueMap().get("magicProp", 23);
    }

}
