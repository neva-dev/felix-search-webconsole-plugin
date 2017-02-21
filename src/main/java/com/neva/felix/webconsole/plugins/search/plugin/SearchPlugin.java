package com.neva.felix.webconsole.plugins.search.plugin;

import org.osgi.framework.BundleContext;

public class SearchPlugin extends AbstractPlugin {

	public static final String LABEL = "search";

	public static final String TITLE = "Search";

	public SearchPlugin(BundleContext bundleContext) {
		super(bundleContext);
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
