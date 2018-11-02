package com.adobe.acs.samples.clientlibs.impl;


import com.adobe.granite.ui.clientlibs.LibraryType;
import com.adobe.granite.ui.clientlibs.script.ScriptProcessor;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.day.text.Text;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processors are invoked by defining on the cq:ClientLibraryFolder node either cssProcessor or jsProcessor String[] properties (or both).
 * Each entry is composed of:
 *    <mode>:<name>;<option=value>*
 *
 *    The mode can be:
 *          pre | default | min | post
 *       pre: Runs first
 *       default | min
 *          default: Runs after pre but before post, and is the equivalent to !min
 *          min: Runs after pre but before post, and is the equivalent to !default
 *       post: Runs last
 *    The name (matched to .getName() below) is the Processor name
 *
 *    Options are optionally specified after the name by key-value pairs delimited by `=`, each set of key-value pairs is delimited by `;`
 *    Note that a ';' must exist between the name and the first option key-value pair as well.
 *
 * cq:ClientLibraryFolder
 * - jsProcessor = [
 *     <mode>:<name>;<option=value>*
 *     pre:sample;option1=value1;option2=value2,
 *     default:sample;option1=value1;option2=value2
 *     min:sample;option1=value1;option2=value2
 *     post:sample;option1=value1;option2=value2
 *  ]
 */
@Component(service = ScriptProcessor.class)
// https://docs.adobe.com/docs/en/aem/6-3/develop/ref/javadoc/com/adobe/granite/ui/clientlibs/script/ScriptProcessor.html
public class SampleScriptProcessor implements ScriptProcessor {
    private static final Logger log = LoggerFactory.getLogger(SampleScriptProcessor.class);

    private static final String NAME = "sample";

    private static Pattern URL_PATTERN= Pattern.compile("url\\(\\s*(['\"]?)([^'\")]*)(['\"]?\\s*)\\)");

    @Nonnull
    @Override
    public String getName() {
        // This name is important in that its used to identify WHEN this processor is invoked via being specified on the cq:ClientLibraryFolder node as described above.
        return NAME;
    }

    /**
     * This method must be called from process(..) yourself! This is NOT automatically called to determine if process(..) will be called.
     *
     * @param type the ClientLibrary Type
     * @return true to accept this LibraryType, false to reject.
     */
    @Override
    public boolean handles(@Nonnull LibraryType type) {
        // Note this method must be called from process(..) yourself! This is NOT automatically called to determine if process(..) will be called.
        // Determine if process(..) should be called for this library; The LibraryType in question is passed to make this determination.
        // https://docs.adobe.com/docs/en/aem/6-3/develop/ref/javadoc/com/adobe/granite/ui/clientlibs/LibraryType.html

        if (LibraryType.CSS.name().equals(type.name())) {
            // Compares is the Types are the same
            // > For CSS this is: CSS
            // > For JavaScript this is: JS
            return true;
        } else if (LibraryType.CSS.contentType.equals(type.contentType)) {
            // Most often, the Content Type (CSS or JS) is checked.
            // > For CSS this is: text/css
            // > For JavaScript this is: application/javascript
            return true;
        } else if (LibraryType.CSS.extension.equals(type.extension)) {
            // Occasionally the extension can be checked.
            // > For CSS this is: .css
            // > For JavaScript this is: .js
            return true;
        }

        return false;
    }

    /**
     * This is where all the work is done in processing the ClientLibrary.
     *
     * @param type the client library type (CSS or JS)
     * @param source The client library to process; you can get the location, the final contents and options.
     * @param output The writer that can expose updates to the client library.
     * @param options The cq:clientLibraryFolder process options.
     * @return true if the output should be used, false if the source should be used as the output.
     * @throws IOException
     */
    @Override
    public boolean process(@Nonnull LibraryType type, @Nonnull ScriptResource source, @Nonnull Writer output, @Nonnull Map<String, String> options) throws IOException {
        if (!handles(type)) {
            // We must perform the handles check ourselves n process at least up to AEM 6.3 GA
            return false;
        }

        log.debug("Source Name: {}", source.getName()); // -> /apps/my-site/clientlibs/clientlib-foo.css
        log.debug("Source Size: {}", source.getSize()); // -> 654932 (size in bytes)
        log.debug("Source Reader: {}", IOUtils.toString(source.getReader())); // -> Text contents of outputed file

        // 1. Read from source.reader()
        // 2. Transform contents from #1
        // 3. Write final data to output

        String externalizerDomain = StringUtils.defaultIfEmpty(options.get("externalizerDomain"), "publish");
        // Put some better error handling if externalizerDomain is not provided

        // Read in the CSS to parse and update
        final String input = IOUtils.toString(source.getReader());
        // Set up a StringBuffer to record the the transformed content
        final StringBuffer transformedOutput = new StringBuffer(input.length());

        // Create a matcher so we can find things that look like URLs
        final Matcher m = URL_PATTERN.matcher(input);

        // Track if we actually make changes or not
        boolean dirty = false;

        // Search the CSS for the URL PATTERN so we can update them
        while (m.find()) {
            // Use the match groups to get the part of the match we want to transform
            final String url = m.group(2);
            // Check to make sure we don't rewrite external links
            if (!StringUtils.startsWithAny(url, new String[]{"//", "http://", "https://" } )) {

                log.debug("Found local url in CSS [ {} ]", url);

                // Convert the relative path to absolute
                String externalUrl = Text.makeCanonicalPath(source.getName() + url);
                // Prefix the URL w the external domain
                externalUrl = externalizerDomain + externalUrl;
                // Replace the match w the updated doamin
                m.appendReplacement(transformedOutput, Matcher.quoteReplacement(externalUrl));
                // Mark as dirty
                dirty = true;
            }
        }

        if (dirty) {
            // If we made changes then write it to the output
            m.appendTail(transformedOutput);
            output.write(transformedOutput.toString());
            output.flush();
        }

        // Return true only if we made changes, false if we want to use what was input to us.
        return dirty;
    }
}
