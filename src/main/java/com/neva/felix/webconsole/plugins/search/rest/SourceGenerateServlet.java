package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchMonitor;
import com.neva.felix.webconsole.plugins.search.core.sourcegenerator.SourceGeneratorJob;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

public class SourceGenerateServlet extends RestServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SourceGenerateServlet.class);

    public static final String ALIAS_NAME = "source-generate";

    private final SearchMonitor<SourceGeneratorJob> monitor;

    private final OsgiExplorer osgiExplorer;

    public SourceGenerateServlet(BundleContext bundleContext) {
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
            final RestParams params = RestParams.from(request);
            final Set<BundleClass> classes = osgiExplorer.findClasses(params.getBundleIds(), params.getBundleClasses());
            final SourceGeneratorJob job = new SourceGeneratorJob(osgiExplorer, classes);

            monitor.start(job);

            writeMessage(response, MessageType.SUCCESS, "Job started properly.", job);
        } catch (Exception e) {
            LOG.error("Cannot search classes.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String jobId = RestParams.from(request).getJobId();
            SourceGeneratorJob job = monitor.get(jobId);

            if (job == null) {
                writeMessage(response, MessageType.ERROR, String.format("Job with ID '%s' is not running so it cannot be polled.", jobId));
            } else {
                writeMessage(response, MessageType.SUCCESS, "Job polled properly.", job);
            }
        } catch (Exception e) {
            LOG.error("Cannot search classes.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String jobId = RestParams.from(request).getJobId();
            SourceGeneratorJob job = monitor.get(jobId);

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

}
