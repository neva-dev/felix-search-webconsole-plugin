package com.neva.felix.webconsole.plugins.search.core.provider;

import com.neva.felix.webconsole.plugins.search.core.BundleScanner;
import com.neva.felix.webconsole.plugins.search.core.SearchParams;
import com.neva.felix.webconsole.plugins.search.core.SearchResult;
import com.neva.felix.webconsole.plugins.search.rest.BundleDownloadServlet;
import com.neva.felix.webconsole.plugins.search.rest.ClassDecompileServlet;
import com.neva.felix.webconsole.plugins.search.utils.BundleUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.neva.felix.webconsole.plugins.search.core.SearchUtils.composeDescription;

public class ClassSearchProvider extends AbstractSearchProvider {

	public static final String LABEL = "Class";

	private static final int RESULT_RANK = 500;

	private static final Logger LOG = LoggerFactory.getLogger(ClassSearchProvider.class);

	public ClassSearchProvider(BundleContext bundleContext) {
		super(bundleContext);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SearchResult> search(SearchParams params) {
		final List<SearchResult> results = Lists.newArrayList();
		if (StringUtils.isBlank(params.getPhrase())) {
			return results;
		}

		for (Bundle bundle : bundleContext.getBundles()) {
			for (final String className : new BundleScanner(bundle).findClassNames(params.getPhrase())) {
				try {
					final SearchResult result = makeResult(bundle, className);
					if (result != null) {
						results.add(result);
					}
				} catch (Exception e) {
					LOG.warn(String.format("Cannot create search result for class '%s' at bundle '%s'",
							className, bundle.getSymbolicName()), e);
				}
			}
		}

		return results;
	}

	private SearchResult makeResult(Bundle bundle, String className) {
		Map<String, Object> descParams = new LinkedHashMap<>();

		descParams.put("bundle", BundleUtils.descriptionWithId(bundle));

		final String description = composeDescription(descParams);
		final SearchResult result = new SearchResult(getLabel(), String.format("class:%s", className), RESULT_RANK, className, description);

		result.getContext().put("consoleUrl", BundleUtils.consolePath(bundleContext, bundle));
		result.getContext().put("bundleDownloadUrl", BundleDownloadServlet.url(bundleContext, bundle));
		result.getContext().put("className", className);
		result.getContext().put("classDecompileUrl", ClassDecompileServlet.url(bundleContext, bundle, className));
		result.getContext().putAll(BundleUtils.context(bundle));

		return result;
	}

	@Override
	public void reset() {
		// nothing to do
	}

	@Override
	public String getLabel() {
		return LABEL;
	}
}
