package com.neva.felix.webconsole.plugins.search;

import com.neva.felix.webconsole.plugins.search.core.SearchService;
import com.neva.felix.webconsole.plugins.search.plugin.AbstractPlugin;
import com.neva.felix.webconsole.plugins.search.plugin.SearchPlugin;
import com.google.common.collect.ImmutableSet;
import com.neva.felix.webconsole.plugins.search.rest.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SearchActivator implements BundleActivator {

	private static final Logger LOG = LoggerFactory.getLogger(SearchActivator.class);

	private final List<ServiceRegistration> services = new LinkedList<>();

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		for (AbstractPlugin plugin : getPlugins(bundleContext)) {
			ServiceRegistration service = bundleContext.registerService(Servlet.class.getName(), plugin, plugin.getProps());
			if (service != null) {
				services.add(service);
			} else {
				LOG.error("Cannot register plugin '{}' as OSGi service", plugin.getClass().getName());
			}
		}
		for (RestServlet servlet : getServlets(bundleContext)) {
			ServiceRegistration service = bundleContext.registerService(Servlet.class.getName(), servlet, servlet.createProps());
			if (service != null) {
				services.add(service);
			} else {
				LOG.error("Cannot register plugin servlet '{}' as OSGi service", servlet.getClass().getName());
			}
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		for (ServiceRegistration service : services) {
			service.unregister();
		}
	}

	private Set<AbstractPlugin> getPlugins(BundleContext bundleContext) {
		return ImmutableSet.of(
				new SearchPlugin(bundleContext)
		);
	}

	private ImmutableSet<RestServlet> getServlets(BundleContext context) {
		return ImmutableSet.of(
				new ByPhraseServlet(context),
				new BundleDownloadServlet(context),
				new BundleClassesServlet(context),
				new ClassDecompileServlet(context),
				new ClassSearchServlet(context),
				new SourceGenerateServlet(context),
				new FileDownloadServlet(context),
				new BundleAssembleServlet(context)
		);
	}
}
