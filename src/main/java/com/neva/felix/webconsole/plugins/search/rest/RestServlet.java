package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.utils.TemplateRenderer;
import jakarta.servlet.http.HttpServlet;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Hashtable;

public abstract class RestServlet extends HttpServlet {

	protected final BundleContext bundleContext;

	protected final TemplateRenderer templateRenderer;

	public RestServlet(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		this.templateRenderer = new TemplateRenderer();
	}

	protected abstract String getAliasName();

	public Dictionary<String, Object> getProps() {
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("alias", getAlias());

		return props;
	}

	public String getAlias() {
		return SearchPaths.from(bundleContext).pluginAlias(getAliasName());
	}

}
