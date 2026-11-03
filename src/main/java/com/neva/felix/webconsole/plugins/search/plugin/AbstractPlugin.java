package com.neva.felix.webconsole.plugins.search.plugin;

import com.google.common.collect.ImmutableMap;
import com.neva.felix.webconsole.plugins.search.rest.RestServlet;
import com.neva.felix.webconsole.plugins.search.utils.TemplateRenderer;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractPlugin extends HttpServlet {

	public static final String CATEGORY = "OSGi";

	protected final BundleContext bundleContext;

	private final Map<String, RestServlet> servlets = new LinkedHashMap<>();

	public AbstractPlugin(BundleContext bundleContext, Collection<RestServlet> servlets) {
		this.bundleContext = bundleContext;
		for (RestServlet servlet : servlets) {
			this.servlets.put(servlet.getAliasName(), servlet);
		}
	}

	public abstract String getLabel();

	public abstract String getTitle();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		for (RestServlet servlet : servlets.values()) {
			servlet.init(config);
		}
	}

	@Override
	public void destroy() {
		for (RestServlet servlet : servlets.values()) {
			servlet.destroy();
		}
		super.destroy();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String alias = StringUtils.removeStart(StringUtils.removeStart(StringUtils.defaultString(request.getPathInfo()), "/" + getLabel()), "/");
		if (alias.isEmpty()) {
			super.service(request, response);
			return;
		}

		final RestServlet servlet = servlets.get(alias);
		if (servlet == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			servlet.service(request, response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String common = TemplateRenderer.render("search/common.html");
		final String content = TemplateRenderer.render(getLabel() + "/plugin.html", ImmutableMap.of("common", common));

		response.getWriter().write(content);
	}

	public Dictionary<String, Object> getProps() {
		final Dictionary<String, Object> props = new Hashtable<>();

		props.put("felix.webconsole.label", getLabel());
		props.put("felix.webconsole.title", getTitle());
		props.put("felix.webconsole.category", CATEGORY);

		return props;
	}
}
