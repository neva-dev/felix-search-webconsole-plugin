package com.neva.felix.webconsole.plugins.search.rest;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

import com.google.common.collect.Lists;
import com.neva.felix.webconsole.plugins.search.core.BundleJar;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchMonitor;
import com.neva.felix.webconsole.plugins.search.core.bundleassemble.BundleAssembleJob;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAssembleServlet extends RestServlet {

    private static final Logger LOG = LoggerFactory.getLogger(BundleAssembleServlet.class);

    public static final String ALIAS_NAME = "bundle-assemble";

    private final SearchMonitor<BundleAssembleJob> monitor;

    private final OsgiExplorer osgiExplorer;

    public BundleAssembleServlet(BundleContext bundleContext) {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            final RestParams params = RestParams.from(request);
            final Set<BundleJar> bundles = osgiExplorer.findBundleJars(params.getBundleIds());
            final BundleAssembleJob job = new BundleAssembleJob(osgiExplorer, bundles);

            monitor.start(job);

            writeMessage(response, MessageType.SUCCESS, "Job started properly.", job);
        } catch (Exception e) {
            LOG.error("Cannot assemble bundles.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RestParams params = RestParams.from(request);
            String jobId = params.getJobId();
            BundleAssembleJob job = monitor.get(jobId);

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
            BundleAssembleJob job = monitor.get(jobId);

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
