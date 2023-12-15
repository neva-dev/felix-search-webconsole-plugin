package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.utils.TemplateRenderer;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

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
		//props.put("alias", getAlias());

		props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/search/" + getAliasName());
		props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "(osgi.http.whiteboard.context.name=org.apache.felix.webconsole)"); // webManagerRoot
		props.put("service.ranking", Integer.MAX_VALUE);

		return props;
	}

	public String getAlias() {
		return "/system/console/search-api/" + getAliasName();
		//return SearchPaths.from(bundleContext).pluginAlias(getAliasName());
	}

}
