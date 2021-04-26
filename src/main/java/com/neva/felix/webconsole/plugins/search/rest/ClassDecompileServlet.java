package com.neva.felix.webconsole.plugins.search.rest;

import com.google.common.collect.ImmutableMap;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompilers;
import com.neva.felix.webconsole.plugins.search.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;

public class ClassDecompileServlet extends RestServlet {

    public static final String ALIAS_NAME = "class-decompile";

    public static final String BUNDLE_ID = "bundleId";

    public static final String CLASS_NAME = "className";

    public static final String DECOMPILER = "decompiler";

    public static final String LINE_NUMBERS = "lineNumbers";

    private final OsgiExplorer osgiExplorer;

    public ClassDecompileServlet(BundleContext bundleContext) {
        super(bundleContext);
        this.osgiExplorer = new OsgiExplorer(bundleContext);
    }

    public static String url(BundleContext context, Bundle bundle, String className) {
        return url(context, bundle.getBundleId(), className);
    }

    public static String url(BundleContext context, long bundleId, String className) {
        return String.format("%s?%s=%d&%s=%s", SearchPaths.from(context).pluginAlias(ALIAS_NAME), BUNDLE_ID, bundleId, CLASS_NAME, className);
    }

    @Override
    protected String getAliasName() {
        return ALIAS_NAME;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String bundleId = StringUtils.trimToEmpty(request.getParameter(BUNDLE_ID));
        final String className = StringUtils.trimToEmpty(request.getParameter(CLASS_NAME));
        final String decompiler = StringUtils.trimToEmpty(request.getParameter(DECOMPILER));
        final String lineNumbers = StringUtils.trimToEmpty(request.getParameter(LINE_NUMBERS));

        final Bundle bundle = bundleContext.getBundle(Long.parseLong(bundleId));
        if (bundle == null) {
            JsonUtils.writeMessage(response, MessageType.ERROR, String.format("Bundle '%s' not be found.", bundleId));
            return;
        }

        final File bundleJar = osgiExplorer.findJar(Long.valueOf(bundleId));
        if (bundleJar == null) {
            JsonUtils.writeMessage(response, MessageType.ERROR, String.format("Bundle '%s' JAR cannot be found.", bundleId));
            return;
        }

        final String classSource = osgiExplorer.decompileClass(Decompilers.valueOf(decompiler), Boolean.parseBoolean(lineNumbers), bundleJar, className);

        JsonUtils.writeMessage(response, MessageType.SUCCESS, "Class details",
                ImmutableMap.of(
                        "className", className,
                        "classSource", classSource,
                        "bundleId", bundleId,
                        "bundleSymbolicName", bundle.getSymbolicName(),
                        "bundleJarPath", bundleJar.getAbsolutePath()
                ));
    }

}
