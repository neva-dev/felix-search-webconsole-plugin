package com.neva.felix.webconsole.plugins.search.rest;

import com.google.common.collect.Lists;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompilers;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * Place for unified param names used in multiple REST endpoints.
 */
public class RestParams {

    public static final String PHRASE_PARAM = "phrase";

    public static final String JOB_ID_PARAM = "jobId";

    public static final String BUNDLE_ID_PROP = "bundleId[]";

    public static final String BUNDLE_CLASS_PROP = "bundleClass[]";

    public static final String PATH_PARAM = "path";

    public static final String NAME_PARAM = "name";

    public static final String DECOMPILER = "decompiler";

    private final HttpServletRequest request;

    public RestParams(HttpServletRequest request) {
        this.request = request;
    }

    public static RestParams from(HttpServletRequest request) {
        return new RestParams(request);
    }

    public String getPhrase() {
        return getString(PHRASE_PARAM);
    }

    public String getJobId() {
        return getString(JOB_ID_PARAM);
    }

    public List<String> getBundleIds() {
        String[] values = request.getParameterValues(BUNDLE_ID_PROP);
        return ArrayUtils.isNotEmpty(values) ? Lists.newArrayList(values) : Collections.<String>emptyList();
    }

    public List<String> getBundleClasses() {
        final String[] values = request.getParameterValues(BUNDLE_CLASS_PROP);

        return ArrayUtils.isEmpty(values) ? Collections.<String>emptyList() : Lists.newArrayList(values);
    }

    public boolean getBoolean(String param) {
        return BooleanUtils.toBoolean(StringUtils.trimToEmpty(request.getParameter(param)));
    }

    public String getString(String param) {
        return StringUtils.trimToEmpty(request.getParameter(param));
    }

    public Decompilers getType() {
        final String decompiler = StringUtils.trimToEmpty(request.getParameter(DECOMPILER));
        return StringUtils.isNoneBlank(decompiler) ? Decompilers.valueOf(decompiler) : Decompilers.JD_CORE;
    }

}
