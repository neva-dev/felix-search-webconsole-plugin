package com.neva.felix.webconsole.plugins.search;

import com.neva.felix.webconsole.plugins.search.plugin.AbstractPlugin;
import com.neva.felix.webconsole.plugins.search.plugin.SearchPlugin;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Set;

public class SearchActivator implements BundleActivator {

	private SearchHttpTracker httpTracker;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		for (AbstractPlugin plugin : getPlugins(bundleContext)) {
			plugin.register();
		}

		httpTracker = new SearchHttpTracker(bundleContext);
		httpTracker.open();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		httpTracker.close();
		httpTracker = null;
	}

	private Set<AbstractPlugin> getPlugins(BundleContext bundleContext) {
		return ImmutableSet.<AbstractPlugin>of(
				new SearchPlugin(bundleContext)
		);
	}
}
