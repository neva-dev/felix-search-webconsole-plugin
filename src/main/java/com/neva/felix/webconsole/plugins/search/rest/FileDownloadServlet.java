package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.utils.io.FileDownloader;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

public class FileDownloadServlet extends RestServlet {

    public static final String ALIAS_NAME = "file-download";

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadServlet.class);

    public FileDownloadServlet(BundleContext bundleContext) {
        super(bundleContext);
    }

    public static String url(BundleContext context, String path, String fileName) {
        return String.format("%s?%s=%s&%s=%s", SearchPaths.from(context).pluginAlias(ALIAS_NAME),
                RestParams.PATH_PARAM, path, RestParams.NAME_PARAM, fileName);
    }

    @Override
    protected String getAliasName() {
        return ALIAS_NAME;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            final RestParams params = RestParams.from(request);
            final String path = params.getString(RestParams.PATH_PARAM);
            final String name = StringUtils.defaultIfBlank(params.getString(RestParams.NAME_PARAM), UUID.randomUUID().toString());

            final File file = new File(path);
            if (!file.exists()) {
                writeMessage(response, MessageType.ERROR, String.format("File at path '%s' cannot be found.", path));
            } else {
                new FileDownloader(response, new FileInputStream(file), name).download();
            }
        } catch (Exception e) {
            LOG.error("Cannot download file.", e);
            writeMessage(response, MessageType.ERROR, "Internal error occurred.");
        }
    }

}
