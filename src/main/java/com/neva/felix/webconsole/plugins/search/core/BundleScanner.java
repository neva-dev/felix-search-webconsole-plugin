package com.neva.felix.webconsole.plugins.search.core;

import com.neva.felix.webconsole.plugins.search.utils.MultimapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class BundleScanner {

	private final Bundle bundle;

	public BundleScanner(Bundle bundle) {
		this.bundle = bundle;
	}

	public List<String> findClassNames() {
		return findClassNames(null);
	}

	public List<String> findClassNames(String phrase) {
		List<String> classNames = Lists.newArrayList();

		@SuppressWarnings("unchecked")
		final Enumeration<URL> classUrls = getUrls();
		if (classUrls != null) {
			while (classUrls.hasMoreElements()) {
				final URL url = classUrls.nextElement();
				final String className = toClassName(url);

				if (phrase == null || SearchUtils.containsPhrase(phrase, className)) {
					classNames.add(className);
				}
			}
		}

		return classNames;
	}

	private Enumeration getUrls() {
		return bundle.findEntries("/", "*.class", true);
	}

	private String toClassName(URL url) {
		final String f = url.getFile();
		final String cn = f.substring(1, f.length() - ".class".length());

		return cn.replace('/', '.');
	}

	public Map<String, Object> composeClassTree() {
		Map<String, Object> tree = Maps.newLinkedHashMap();

		@SuppressWarnings("unchecked")
		final Enumeration<URL> classUrls = getUrls();
		if (classUrls != null) {
			while (classUrls.hasMoreElements()) {
				final URL url = classUrls.nextElement();
				String path = StringUtils.removeStart(url.getFile(), "/");
				MultimapUtil.put(tree, path, toClassName(url));
			}
		}

		return tree;
	}

}
