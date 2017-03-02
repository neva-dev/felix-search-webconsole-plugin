package com.neva.felix.webconsole.plugins.search.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.osgi.framework.Bundle;

public class BundleClass {

    private Bundle bundle;

    private String className;

    public BundleClass(Bundle bundle, String className) {
        this.bundle = bundle;
        this.className = className;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getClassName() {
        return className;
    }

    public String getClassPath() {
        return StringUtils.replace(className, ".", "/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleClass that = (BundleClass) o;

        return new EqualsBuilder()
                .append(bundle, that.bundle)
                .append(className, that.className)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(bundle)
                .append(className)
                .toHashCode();
    }
}
