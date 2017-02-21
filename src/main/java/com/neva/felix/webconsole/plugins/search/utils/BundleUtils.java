package com.neva.felix.webconsole.plugins.search.utils;

import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.util.Map;

public class BundleUtils {

	public static String consolePath(BundleContext context, Bundle bundle) {
		return consolePath(context, bundle.getBundleId());
	}

	public static String consolePath(BundleContext context, long bundleId) {
		return SearchPaths.from(context).appAlias("bundles") + "/" + String.valueOf(bundleId);
	}

	public static String mapState(int state) {
		switch (state) {
			case Bundle.ACTIVE:
				return "active";
			case Bundle.INSTALLED:
				return "installed";
			case Bundle.RESOLVED:
				return "resolved";
			case Bundle.UNINSTALLED:
				return "uninstalled";
			default:
				return "unknown";
		}
	}

	public static Map<String, String> context(Bundle bundle) {
		return ImmutableMap.of(
				"bundleId", String.valueOf(bundle.getBundleId()),
				"bundleDescription", description(bundle)
		);
	}

	public static String description(Bundle bundle) {
		return StringUtils.defaultString((String) bundle.getHeaders().get(Constants.BUNDLE_NAME), bundle.getSymbolicName());
	}

	public static String descriptionWithId(Bundle bundle) {
		return String.format("%s (%d)", description(bundle), bundle.getBundleId());
	}


}
