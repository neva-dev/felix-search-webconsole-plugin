package com.neva.felix.webconsole.plugins.search.plugin;

import com.google.common.collect.ImmutableMap;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.felix.webconsole.servlet.AbstractServlet;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

public abstract class AbstractPlugin extends AbstractServlet {

	public static final String CATEGORY = "OSGi";

	protected final BundleContext bundleContext;

	public AbstractPlugin(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void renderContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String common = readTemplateFile("/search/common.html");
		final String specific = readTemplateFile("/" + getLabel() + "/plugin.html");
		final String content = StrSubstitutor.replace(specific, ImmutableMap.of("common", common));

		response.getWriter().write(content);
	}

	public abstract String getLabel();

	public abstract String getTitle();

	public Dictionary<String, Object> getProps() {
		final Dictionary<String, Object> props = new Hashtable<>();

		props.put("felix.webconsole.label", getLabel());
		props.put("felix.webconsole.category", CATEGORY);

		return props;
	}
}
