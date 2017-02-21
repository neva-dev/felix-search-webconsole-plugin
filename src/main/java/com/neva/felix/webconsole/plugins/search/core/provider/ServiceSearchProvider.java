package com.neva.felix.webconsole.plugins.search.core.provider;

import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.MetaTypeBundleClass;
import com.neva.felix.webconsole.plugins.search.core.SearchParams;
import com.neva.felix.webconsole.plugins.search.core.SearchResult;
import com.neva.felix.webconsole.plugins.search.core.SearchUtils;
import com.neva.felix.webconsole.plugins.search.rest.BundleDownloadServlet;
import com.neva.felix.webconsole.plugins.search.rest.ClassDecompileServlet;
import com.neva.felix.webconsole.plugins.search.utils.BundleUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceSearchProvider extends AbstractSearchProvider {

	public static final String LABEL = "Service";

	private static final Logger LOG = LoggerFactory.getLogger(ClassSearchProvider.class);

	private static final int RESULT_RANK = 400;

	private final OsgiExplorer osgiExplorer;

	public ServiceSearchProvider(BundleContext bundleContext) {
		super(bundleContext);
		this.osgiExplorer = new OsgiExplorer(bundleContext);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SearchResult> search(SearchParams params) {
		final List<SearchResult> results = Lists.newArrayList();
		if (StringUtils.isBlank(params.getPhrase())) {
			return results;
		}

		for (ServiceReference serviceReference : osgiExplorer.getServiceReferences()) {
			try {
				final SearchResult result = makeResult(params, serviceReference);
				if (result != null) {
					results.add(result);
				}
			} catch (Exception e) {
				LOG.warn("Cannot create a search result for service reference", e);
			}
		}

		return results;
	}

	private SearchResult makeResult(SearchParams params, ServiceReference serviceReference) {
		final Bundle bundle = serviceReference.getBundle();
		final Long id = (Long) serviceReference.getProperty(Constants.SERVICE_ID);
		final String pid = StringUtils.defaultString((String) serviceReference.getProperty(Constants.SERVICE_PID));
		final ObjectClassDefinition classDef = osgiExplorer.getClassDefinition(bundle, pid);
		final BundleClass clazz = osgiExplorer.findClass(serviceReference);
		final List<String> objectClasses = getObjectClasses(serviceReference);

		String label = StringUtils.defaultIfBlank(pid, String.format("Service ID %d", id));
		if (classDef != null) {
			label = StringUtils.defaultIfBlank(classDef.getName(), label);
		} else if (clazz != null) {
			label = clazz.getClassName();
		}

		final List<String> texts = ImmutableList.<String>builder().add(label).add(pid).addAll(objectClasses).build();
		if (!SearchUtils.containsPhrase(params.getPhrase(), texts)) {
			return null;
		}

		final String description = composeDescription(serviceReference, clazz);
		final SearchResult result = new SearchResult(getLabel(), String.format("service:%d", id), RESULT_RANK, label, description);

		result.getContext().put("consoleUrl", SearchPaths.from(bundleContext).appAlias("services") + "/" + String.valueOf(id));
		result.getContext().put("bundleDownloadUrl", BundleDownloadServlet.url(bundleContext, bundle));
		result.getContext().putAll(BundleUtils.context(bundle));

		if (!pid.isEmpty()) {
			result.addPhrase(StringUtils.substringAfterLast(pid, "."));
		}

		if (clazz != null) {
			result.getContext().put("className", clazz.getClassName());
			result.getContext().put("classDecompileUrl", ClassDecompileServlet.url(bundleContext, clazz.getBundle(), clazz.getClassName()));
		}

		return result;
	}

	private String composeDescription(ServiceReference serviceReference, BundleClass clazz) {
		final Map<String, Object> descParams = new LinkedHashMap<>();

		descParams.put("bundle", BundleUtils.descriptionWithId(clazz.getBundle()));

		if (clazz instanceof MetaTypeBundleClass) {
			ObjectClassDefinition definition = ((MetaTypeBundleClass) clazz).getDefinition();
			descParams.put("description", definition.getDescription());
		}

		for (String propName : serviceReference.getPropertyKeys()) {
			Object value = serviceReference.getProperty(propName);
			if (value != null) {
				descParams.put(propName, value);
			}
		}

		return SearchUtils.composeDescription(descParams);
	}

	private List<String> getObjectClasses(ServiceReference serviceReference) {
		String[] values = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);

		return values != null ? Lists.newArrayList(values) : Collections.<String>emptyList();
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public void reset() {
		// nothing to do
	}
}
