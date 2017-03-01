package com.neva.felix.webconsole.plugins.search.rest;

import com.google.common.collect.Lists;
import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchMonitor;
import com.neva.felix.webconsole.plugins.search.core.classsearch.ClassSearchJob;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

public class ClassSearchServlet extends RestServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ClassSearchServlet.class);

    public static final String ALIAS_NAME = "class-search";

    private static final String PHRASE_PARAM = "phrase";

    private static final String JOB_ID_PARAM = "jobId";

    private static final String BUNDLE_ID_PROP = "bundleId[]";

    private static final String BUNDLE_CLASS_PROP = "bundleClass[]";

    private final SearchMonitor<ClassSearchJob> monitor;

    private final OsgiExplorer osgiExplorer;

    public ClassSearchServlet(BundleContext bundleContext) {
        super(bundleContext);
        this.monitor = new SearchMonitor<>();
        this.osgiExplorer = new OsgiExplorer(bundleContext);
    }

    @Override
    protected String getAliasName() {
        return ALIAS_NAME;
    }

    @Override
    public void destroy() {
        monitor.shutdown();
        super.destroy();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            final String phrase = getPhrase(request);
            final Set<BundleClass> classes = osgiExplorer.findClasses(getBundleIds(request), getBundleClasses(request));
            final ClassSearchJob job = new ClassSearchJob(osgiExplorer, phrase, classes);

            monitor.start(job);

            writeMessage(response, MessageType.SUCCESS, "Job started properly.", job);
        } catch (Exception e) {
            LOG.error("Cannot search classes.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String jobId = getJobId(request);
            ClassSearchJob job = monitor.get(jobId);

            if (job == null) {
                writeMessage(response, MessageType.ERROR, String.format("Job with ID '%s' is not running so it cannot be polled.", jobId));
            } else {
                job.poll();
                writeMessage(response, MessageType.SUCCESS, "Job polled properly.", job);
            }
        } catch (Exception e) {
            LOG.error("Cannot search classes.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String jobId = getJobId(request);
            ClassSearchJob job = monitor.get(jobId);

            if (job == null) {
                writeMessage(response, MessageType.ERROR, String.format("Job with ID '%s' is not running so it cannot be stopped.", jobId));
            } else {
                monitor.stop(job);
                writeMessage(response, MessageType.SUCCESS, "Job stopped properly.", job);
            }
        } catch (Exception e) {
            LOG.error("Cannot search classes.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    private String getPhrase(HttpServletRequest request) {
        return StringUtils.trimToEmpty(request.getParameter(PHRASE_PARAM));
    }

    private String getJobId(HttpServletRequest request) {
        return StringUtils.trimToEmpty(request.getParameter(JOB_ID_PARAM));
    }

    private List<String> getBundleIds(HttpServletRequest request) {
        String[] values = request.getParameterValues(BUNDLE_ID_PROP);
        return ArrayUtils.isNotEmpty(values) ? Lists.newArrayList(values) : Collections.<String>emptyList();
    }

    private List<String> getBundleClasses(HttpServletRequest request) {
        final String[] values = request.getParameterValues(BUNDLE_CLASS_PROP);

        return ArrayUtils.isEmpty(values) ? Collections.<String>emptyList() : Lists.newArrayList(values);
    }

}
