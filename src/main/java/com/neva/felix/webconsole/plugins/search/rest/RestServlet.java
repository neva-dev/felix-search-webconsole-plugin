package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.utils.TemplateRenderer;
import org.osgi.framework.BundleContext;

import jakarta.servlet.http.HttpServlet;

public abstract class RestServlet extends HttpServlet {

	protected final BundleContext bundleContext;

	protected final TemplateRenderer templateRenderer;

	public RestServlet(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		this.templateRenderer = new TemplateRenderer();
	}

	public abstract String getAliasName();
}
