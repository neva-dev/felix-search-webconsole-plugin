package com.neva.felix.webconsole.plugins.search;

import com.neva.felix.webconsole.plugins.search.rest.*;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.Set;

public class SearchHttpTracker extends ServiceTracker {

	private static final Logger LOG = LoggerFactory.getLogger(SearchHttpTracker.class);

	private final Set<RestServlet> restServlets;

	public SearchHttpTracker(BundleContext context) {
		super(context, HttpService.class.getName(), null);
		this.restServlets = createRestServlets();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		HttpService httpService = (HttpService) super.addingService(reference);
		if (httpService == null)
			return null;

		try {
			for (RestServlet restServlet : restServlets) {
				httpService.registerServlet(restServlet.getAlias(), restServlet, restServlet.createProps(), null);
			}
		} catch (NamespaceException | ServletException e) {
			LOG.error("Cannot register REST servlet for search webconsole plugin.", e);
		}

		return httpService;
	}

	public void removedService(ServiceReference reference, Object service) {
		HttpService httpService = (HttpService) service;
		for (RestServlet restServlet : restServlets) {
			httpService.unregister(restServlet.getAlias());
		}

		super.removedService(reference, service);
	}

	private ImmutableSet<RestServlet> createRestServlets() {
		return ImmutableSet.of(
				new ByPhraseServlet(context),
				new BundleDownloadServlet(context),
				new BundleClassesServlet(context),
				new ClassDecompileServlet(context),
				new ClassSearchServlet(context),
				new SourceGenerateServlet(context)
		);
	}
}
