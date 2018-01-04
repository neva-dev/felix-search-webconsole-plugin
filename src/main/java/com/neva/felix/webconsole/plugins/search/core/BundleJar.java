package com.neva.felix.webconsole.plugins.search.core;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.osgi.framework.Bundle;

public class BundleJar {

    private Bundle bundle;

    private File jar;

    public BundleJar(Bundle bundle, File jar) {
        this.bundle = bundle;
        this.jar = jar;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public File getJar() {
        return jar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleJar that = (BundleJar) o;

        return new EqualsBuilder()
                .append(bundle, that.bundle)
                .append(jar, that.jar)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(bundle)
                .append(jar)
                .toHashCode();
    }
}
