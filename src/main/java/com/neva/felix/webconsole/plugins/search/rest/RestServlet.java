package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.utils.TemplateRenderer;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServlet;
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

	public Dictionary<String, Object> createProps() {
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("osgi.http.whiteboard.servlet.pattern", "/search/" + getAliasName());
		props.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=org.apache.felix.webconsole)");
		return props;
	}
}
