package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.utils.JsonUtils;
import com.neva.felix.webconsole.plugins.search.utils.io.FileDownloader;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;

public class BundleDownloadServlet extends RestServlet {

    public static final String ALIAS_NAME = "bundle-download";

    public static final String ID_PARAM = "id";

    private final OsgiExplorer osgiExplorer;

    public BundleDownloadServlet(BundleContext bundleContext) {
        super(bundleContext);
        this.osgiExplorer = new OsgiExplorer(bundleContext);
    }

    public static String url(BundleContext context, Bundle bundle) {
        return url(context, bundle.getBundleId());
    }

    public static String url(BundleContext context, long bundleId) {
        return String.format(SearchPaths.from(context).pluginAlias(ALIAS_NAME) + "?" + ID_PARAM + "=%d", bundleId);
    }

    @Override
    protected String getAliasName() {
        return ALIAS_NAME;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String bundleId = StringUtils.trimToEmpty(request.getParameter(ID_PARAM));

        final File jar = osgiExplorer.findJar(Long.valueOf(bundleId));
        if (jar == null) {
            JsonUtils.writeMessage(response, MessageType.ERROR, String.format("Bundle '%s' JAR cannot be found.", bundleId));
            return;
        }

        final String jarName = osgiExplorer.proposeJarName(bundleId);
        final FileDownloader downloader = new FileDownloader(response, new FileInputStream(jar), jarName);

        downloader.download();
    }

}
