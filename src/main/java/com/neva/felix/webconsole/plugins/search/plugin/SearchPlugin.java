package com.neva.felix.webconsole.plugins.search.plugin;

import com.neva.felix.webconsole.plugins.search.rest.RestServlet;
import org.osgi.framework.BundleContext;

import java.util.Collection;

public class SearchPlugin extends AbstractPlugin {

	public static final String LABEL = "search";

	public static final String TITLE = "Search";

	public SearchPlugin(BundleContext bundleContext, Collection<RestServlet> servlets) {
		super(bundleContext, servlets);
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

}
