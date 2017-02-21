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
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationSearchProvider extends AbstractSearchProvider {

	public static final String LABEL = "Configuration";

	private static final Logger LOG = LoggerFactory.getLogger(ClassSearchProvider.class);

	private static final int RESULT_RANK = 350;

	private final OsgiExplorer osgiExplorer;

	public ConfigurationSearchProvider(BundleContext bundleContext) {
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

		for (Configuration configuration : osgiExplorer.getConfigurations()) {
			try {
				final SearchResult result = makeResult(params, configuration);
				if (result != null) {
					results.add(result);
				}
			} catch (Exception e) {
				LOG.warn(String.format("Cannot create a search result from configuration '%s'",
						configuration.getPid()), e);
			}
		}

		return results;
	}

	private SearchResult makeResult(SearchParams params, Configuration configuration) {
		final String pid = configuration.getPid();
		final String factoryPid = StringUtils.defaultString(configuration.getFactoryPid());
		final String classPid = StringUtils.defaultIfBlank(factoryPid, pid);
		final BundleClass clazz = osgiExplorer.findClass(configuration);

		String label = classPid;
		if (clazz instanceof MetaTypeBundleClass) {
			label = StringUtils.defaultIfBlank(((MetaTypeBundleClass) clazz).getDefinition().getName(), label);
		}

		final List<String> texts = ImmutableList.<String>builder().add(label).add(pid).add(factoryPid).build();
		if (!SearchUtils.containsPhrase(params.getPhrase(), texts)) {
			return null;
		}

		final String description = composeDescription(configuration, clazz);
		final SearchResult result = new SearchResult(getLabel(), String.format("configuration:%s", pid), RESULT_RANK, label, description);

		result.getContext().put("consoleUrl", SearchPaths.from(bundleContext).appAlias("configMgr") + "/" + pid);
		result.getContext().put("bundleDownloadUrl", BundleDownloadServlet.url(bundleContext, clazz.getBundle()));
		result.getContext().put("className", clazz.getClassName());
		result.getContext().put("classDecompileUrl", ClassDecompileServlet.url(bundleContext, clazz.getBundle(), clazz.getClassName()));
		result.getContext().putAll(BundleUtils.context(clazz.getBundle()));

		if (!pid.isEmpty()) {
			result.addPhrase(StringUtils.substringAfterLast(pid, "."));
		}

		return result;
	}

	private String composeDescription(Configuration configuration, BundleClass clazz) {
		final Map<String, Object> descParams = new LinkedHashMap<>();

		descParams.put("bundle", BundleUtils.descriptionWithId(clazz.getBundle()));

		if (clazz instanceof MetaTypeBundleClass) {
			ObjectClassDefinition definition = ((MetaTypeBundleClass) clazz).getDefinition();
			descParams.put("description", definition.getDescription());
		}

		Enumeration enumeration = configuration.getProperties().keys();
		while (enumeration.hasMoreElements()) {
			String propName = (String) enumeration.nextElement();
			Object propValue = configuration.getProperties().get(propName);

			if (propValue != null) {
				descParams.put(propName, propValue);
			}
		}

		return SearchUtils.composeDescription(descParams);
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
