package com.neva.felix.webconsole.plugins.search.core;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;

public class SearchPaths {

	public static final String APP_NAME = "search";

	private static final String APP_ROOT_PROP = "felix.webconsole.manager.root";

	private static final String APP_ROOT_DEFAULT = "/system/console";

	private final BundleContext context;

	public SearchPaths(BundleContext context) {
		this.context = context;
	}

	public static SearchPaths from(BundleContext context) {
		return new SearchPaths(context);
	}

	public String appRoot() {
		return StringUtils.defaultIfEmpty(context.getProperty(APP_ROOT_PROP), APP_ROOT_DEFAULT);
	}

	public String appAlias(String alias) {
		return appRoot() + "/" + alias;
	}

	public String pluginRoot() {
		return appRoot() + "/" + APP_NAME;
	}

	public String pluginAlias(String alias) {
		return pluginRoot() + "/" + alias;
	}
}
