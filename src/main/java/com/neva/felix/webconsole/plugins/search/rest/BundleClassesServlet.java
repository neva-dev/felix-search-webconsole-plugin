package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.SearchPaths;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.BundleScanner;
import com.neva.felix.webconsole.plugins.search.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;

public class BundleClassesServlet extends RestServlet {

	public static final String ALIAS_NAME = "bundle-classes";

	public static final String BUNDLE_ID = "bundleId";

	private final OsgiExplorer osgiExplorer;

	public BundleClassesServlet(BundleContext bundleContext) {
		super(bundleContext);
		this.osgiExplorer = new OsgiExplorer(bundleContext);
	}

	public static String url(BundleContext context, Bundle bundle) {
		return url(context, bundle.getBundleId());
	}

	public static String url(BundleContext context, long bundleId) {
		return String.format("%s?%s=%d", SearchPaths.from(context).pluginAlias(ALIAS_NAME), BUNDLE_ID, bundleId);
	}

	@Override
	protected String getAliasName() {
		return ALIAS_NAME;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String bundleId = StringUtils.trimToEmpty(request.getParameter(BUNDLE_ID));
		final Bundle bundle = bundleContext.getBundle(Long.valueOf(bundleId));
		if (bundle == null) {
			JsonUtils.writeMessage(response, MessageType.ERROR, String.format("Bundle '%s' not be found.", bundleId));
			return;
		}

		final File bundleJar = osgiExplorer.findJar(Long.valueOf(bundleId));
		if (bundleJar == null) {
			JsonUtils.writeMessage(response, MessageType.ERROR, String.format("Bundle '%s' JAR cannot be found.", bundleId));
			return;
		}

		final Map<String, Object> classes = composeClassTree(bundle);

		JsonUtils.writeMessage(response, MessageType.SUCCESS, "Bundle classes found",
				ImmutableMap.of(
						"bundleId", bundleId,
						"bundleSymbolicName", bundle.getSymbolicName(),
						"bundleJarPath", bundleJar.getAbsolutePath(),
						"classes", classes
				));
	}

	private Map<String, Object> composeClassTree(Bundle bundle) {
		final Map<String, Object> classTree = new BundleScanner(bundle).composeClassTree();
		final Counter counter = new Counter();
		final List<Object> tree = formatClassTree(bundle, classTree, counter);

		return ImmutableMap.<String, Object>builder()
				.put("tree", tree)
				.put("count", counter.count)
				.build();
	}

	@SuppressWarnings("unchecked")
	private List<Object> formatClassTree(Bundle bundle, Map<String, Object> root, Counter counter) {
		List<Object> result = Lists.newArrayList();

		for (Map.Entry<String, Object> entry : root.entrySet()) {
			Map<String, Object> level = Maps.newLinkedHashMap();
			Object value = entry.getValue();

			if (value instanceof Map) {
				level.put("name", entry.getKey());
				level.put("children", formatClassTree(bundle, (Map) value, counter));
			} else {
				level.put("name", entry.getKey());
				level.put("decompileUrl", ClassDecompileServlet.url(bundleContext, bundle, (String) entry.getValue()));
				counter.increment();
			}

			result.add(level);
		}

		return result;
	}

	private class Counter {
		int count = 0;

		void increment() {
			count++;
		}
	}

}
