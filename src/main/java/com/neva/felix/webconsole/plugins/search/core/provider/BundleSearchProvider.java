package com.neva.felix.webconsole.plugins.search.core.provider;

import com.neva.felix.webconsole.plugins.search.core.SearchParams;
import com.neva.felix.webconsole.plugins.search.core.SearchResult;
import com.neva.felix.webconsole.plugins.search.core.SearchUtils;
import com.neva.felix.webconsole.plugins.search.rest.BundleClassesServlet;
import com.neva.felix.webconsole.plugins.search.rest.BundleDownloadServlet;
import com.neva.felix.webconsole.plugins.search.utils.BundleUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BundleSearchProvider extends AbstractSearchProvider {

	public static final String LABEL = "Bundle";

	private static final int RESULT_RANK = 350;

	private static final Logger LOG = LoggerFactory.getLogger(BundleSearchProvider.class);

	public BundleSearchProvider(BundleContext bundleContext) {
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
			try {
				final SearchResult result = makeResult(params, bundle);
				if (result != null) {
					results.add(result);
				}
			} catch (Exception e) {
				LOG.warn(String.format("Cannot create search result for bundle '%s'", bundle.getSymbolicName()), e);
			}
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	private SearchResult makeResult(SearchParams params, Bundle bundle) {
		final Dictionary<String, String> headers = bundle.getHeaders();
		final String bundleName = headers.get(Constants.BUNDLE_NAME);
		final String symbolicName = bundle.getSymbolicName();
		final long id = bundle.getBundleId();

		final List<String> texts = Arrays.asList(bundleName, symbolicName,
				String.valueOf(id));

		if (!SearchUtils.containsPhrase(params.getPhrase(), texts)) {
			return null;
		}

		final String name = StringUtils.isNotBlank(bundleName) ? bundleName : symbolicName;

		Map<String, Object> descParams = new LinkedHashMap<>();
		descParams.put("symbolic name", bundle.getSymbolicName());
		descParams.put("state", BundleUtils.mapState(bundle.getState()));
		descParams.put("id", id);

		final String description = SearchUtils.composeDescription(descParams);
		final SearchResult result = new SearchResult(getLabel(), String.format("bundle:%d", id), RESULT_RANK, name, description);

		result.addPhrase(StringUtils.substringAfterLast(symbolicName, "."));
		result.getContext().put("consoleUrl", BundleUtils.consolePath(bundleContext, bundle));
		result.getContext().put("bundleDownloadUrl", BundleDownloadServlet.url(bundleContext, bundle));
		result.getContext().put("bundleClassesUrl", BundleClassesServlet.url(bundleContext, bundle));
		result.getContext().putAll(BundleUtils.context(bundle));

		return result;
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
