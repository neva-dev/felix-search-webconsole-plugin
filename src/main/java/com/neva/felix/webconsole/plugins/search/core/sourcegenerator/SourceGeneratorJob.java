package com.neva.felix.webconsole.plugins.search.core.sourcegenerator;

import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchJob;

import java.util.Set;

public class SourceGeneratorJob extends SearchJob {

    private transient final Set<BundleClass> classes;

    private String zipPath;

    public SourceGeneratorJob(OsgiExplorer osgiExplorer, Set<BundleClass> classes) {
        super(osgiExplorer);
        this.classes = classes;
        this.step = "Gathering";
    }

    @Override
    public void perform() {
        total = classes.size();
        step = "Building ZIP";

        // create ZIP at temp dir

        for (BundleClass clazz : classes) {
            // add ZIP entry with file and decompiled source

            increment();
        }

        // when building complete, force download and use zip path in servlet
    }

    public String getZipPath() {
        return zipPath;
    }

}
