package com.neva.felix.webconsole.plugins.search.core;

import org.osgi.framework.Bundle;
import org.osgi.service.metatype.ObjectClassDefinition;

public class MetaTypeBundleClass extends BundleClass {

	private final ObjectClassDefinition definition;

	public MetaTypeBundleClass(Bundle bundle, String className, ObjectClassDefinition definition) {
		super(bundle, className);
		this.definition = definition;
	}

	public MetaTypeBundleClass(BundleClass base, ObjectClassDefinition definition) {
		this(base.getBundle(), base.getClassName(), definition);
	}

	public ObjectClassDefinition getDefinition() {
		return definition;
	}
}
