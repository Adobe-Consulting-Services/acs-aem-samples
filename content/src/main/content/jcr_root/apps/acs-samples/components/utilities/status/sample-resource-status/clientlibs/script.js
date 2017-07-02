(function(jQuery, document) {
    'use strict';

    /**  GraniteUI elements available via: https://docs.adobe.com/docs/en/aem/6-2/develop/ref/granite-ui/api/jcr_root/libs/granite/ui/index.html **/

    /**
     * Bind click handlers to the Actions via selectors. The action link will have a "data-status-action-id" set to the "actionId" of the Action of the ResourceStatus object.
     * See: com.adobe.acs.samples.resourcestatus.impl.SampleEditorResourceStatusProvider
     **/
    $(document).on('click', '.editor-StatusBar-action[data-status-action-id="do-something"]', function () {
        var foundationUI = $(window).adaptTo("foundation-ui");

        foundationUI.prompt(
            "Do something",
            "This is a prompt about something. This could be any control or GraniteUI artifact. https://docs.adobe.com/docs/en/aem/6-2/develop/ref/granite-ui/api/jcr_root/libs/granite/ui/index.html",
            "success",
            [{
                text: "OK",
                primary: true
            }]
        );
    });

    $(document).on('click', '.editor-StatusBar-action[data-status-action-id="do-something-else"]', function () {
        var foundationUI = $(window).adaptTo("foundation-ui");

        foundationUI.prompt("Do something else",
            "This is a prompt about something else. This could make XHR call back to AEM to get more data. https://docs.adobe.com/docs/en/aem/6-2/develop/ref/granite-ui/api/jcr_root/libs/granite/ui/index.html",
            "warning",
            [{
                text: "OK",
                primary: true
            }]
        );
    });

}(jQuery, document));