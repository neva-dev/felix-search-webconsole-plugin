package com.neva.felix.webconsole.plugins.search;

import com.neva.felix.webconsole.plugins.search.plugin.AbstractPlugin;
import com.neva.felix.webconsole.plugins.search.plugin.SearchPlugin;
import com.google.common.collect.ImmutableSet;
import com.neva.felix.webconsole.plugins.search.rest.*;
import jakarta.servlet.Servlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SearchActivator implements BundleActivator {

	private final List<ServiceRegistration> services = new LinkedList<>();

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		for (AbstractPlugin plugin : getPlugins(bundleContext)) {
			ServiceRegistration service = bundleContext.registerService(Servlet.class.getName(), this, plugin.getProps());
			if (service != null) {
				services.add(service);
			}
		}
		for (RestServlet servlet : getServlets(bundleContext)) {
			ServiceRegistration service = bundleContext.registerService(Servlet.class.getName(), servlet, servlet.getProps());
			if (service != null) {
				services.add(service);
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
