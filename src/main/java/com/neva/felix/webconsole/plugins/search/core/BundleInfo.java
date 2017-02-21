package com.neva.felix.webconsole.plugins.search.core;

import com.neva.felix.webconsole.plugins.search.rest.BundleClassesServlet;
import com.neva.felix.webconsole.plugins.search.rest.BundleDownloadServlet;
import com.neva.felix.webconsole.plugins.search.utils.BundleUtils;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.io.Serializable;
import java.util.Map;

public class BundleInfo implements Serializable {

	private final String symbolicName;

	private final String name;

	private final long id;

	private final Map<String, String> context;

	public BundleInfo(Bundle bundle, BundleContext context) {
		this.symbolicName = bundle.getSymbolicName();
		this.name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
		this.id = bundle.getBundleId();
		this.context = ImmutableMap.of(
				"consoleUrl", BundleUtils.consolePath(context, bundle),
				"bundleDownloadUrl", BundleDownloadServlet.url(context, bundle),
				"bundleClassesUrl", BundleClassesServlet.url(context, bundle)
		);
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public Map<String, String> getContext() {
		return context;
	}
}
