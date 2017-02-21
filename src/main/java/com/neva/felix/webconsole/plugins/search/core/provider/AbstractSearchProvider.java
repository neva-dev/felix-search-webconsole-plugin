package com.neva.felix.webconsole.plugins.search.core.provider;

import com.neva.felix.webconsole.plugins.search.core.SearchProvider;
import org.osgi.framework.BundleContext;

public abstract class AbstractSearchProvider implements SearchProvider {

	protected final BundleContext bundleContext;

	public AbstractSearchProvider(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
