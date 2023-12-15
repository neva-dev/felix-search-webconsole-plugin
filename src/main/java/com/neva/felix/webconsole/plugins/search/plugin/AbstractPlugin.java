package com.neva.felix.webconsole.plugins.search.plugin;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

public abstract class AbstractPlugin extends AbstractWebConsolePlugin {

	public static final String CATEGORY = "OSGi";

	protected final BundleContext bundleContext;

	public AbstractPlugin(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	protected void renderContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String common = readTemplateFile("/search/common.html");
		final String specific = readTemplateFile("/" + getLabel() + "/plugin.html");
		final String content = StrSubstitutor.replace(specific, ImmutableMap.of("common", common));

		response.getWriter().write(content);
	}

	public Dictionary<String, Object> getProps() {
		final Dictionary<String, Object> props = new Hashtable<>();

		props.put("felix.webconsole.label", getLabel());
		props.put("felix.webconsole.category", CATEGORY);

		return props;
	}


	// do not remove it - https://felix.apache.org/documentation/subprojects/apache-felix-web-console/extending-the-apache-felix-web-console/providing-resources.html
	public URL getResource(final String path) {
		String prefix = "/" + getLabel() + "/";
		if (path.startsWith(prefix)) {
			return this.getClass().getResource(path);
		}

		return null;
	}
}
