package com.neva.felix.webconsole.plugins.search.core.classsearch;

import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.rest.ClassDecompileServlet;
import org.osgi.framework.BundleContext;

import java.io.Serializable;
import java.util.List;

public class ClassSearchResult implements Serializable {

	private final long bundleId;

	private final String className;

	private final List<String> contexts;

	private final String decompileUrl;

	public ClassSearchResult(BundleContext context, BundleClass clazz, List<String> contexts) {
		this.bundleId = clazz.getBundle().getBundleId();
		this.className = clazz.getClassName();
		this.contexts = contexts;
		this.decompileUrl = ClassDecompileServlet.url(context, clazz.getBundle(), clazz.getClassName());
	}

	public long getBundleId() {
		return bundleId;
	}

	public String getClassName() {
		return className;
	}

	public String getDecompileUrl() {
		return decompileUrl;
	}

	public List<String> getContexts() {
		return contexts;
	}
}
