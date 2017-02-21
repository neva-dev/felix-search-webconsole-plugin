package com.neva.felix.webconsole.plugins.search.utils;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class TemplateRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateRenderer.class);

    protected final Map<String, String> globalVars;

    public TemplateRenderer() {
        this(Collections.<String, String>emptyMap());
    }

    public TemplateRenderer(Map<String, String> globalVars) {
        this.globalVars = globalVars;
    }

    public static String render(String templateFile) {
        return render(templateFile, Collections.<String, String>emptyMap());
    }

    public static String render(String templateFile, Map<String, String> vars) {
        return new TemplateRenderer().renderTemplate(templateFile, vars);
    }

    public final String renderTemplate(String templateFile) {
        return renderTemplate(templateFile, Collections.<String, String>emptyMap());
    }

    public final String renderTemplate(String templateFile, Map<String, String> vars) {
        String result = null;

        InputStream templateStream = getClass().getResourceAsStream("/" + templateFile);
        if (templateStream == null) {
            LOG.error(String.format("Template '%s' cannot be found.", templateFile));
        } else {
            try {
                result = IOUtils.toString(templateStream, "UTF-8");
            } catch (IOException e) {
                LOG.error(String.format("Cannot load template '%s'", templateFile), e);
            } finally {
                IOUtils.closeQuietly(templateStream);
            }
        }

        final Map<String, String> allVars = ImmutableMap.<String, String>builder()
                .putAll(globalVars)
                .putAll(vars)
                .build();

        return StrSubstitutor.replace(result, allVars);
    }

}
