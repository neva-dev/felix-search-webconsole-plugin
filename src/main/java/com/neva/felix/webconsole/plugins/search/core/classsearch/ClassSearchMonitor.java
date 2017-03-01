package com.neva.felix.webconsole.plugins.search.core.classsearch;

import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchMonitor;
import org.osgi.framework.Bundle;

import java.util.List;

public class ClassSearchMonitor extends SearchMonitor<ClassSearchJob> {

    private final OsgiExplorer osgiExplorer;

    public ClassSearchMonitor(OsgiExplorer osgiExplorer) {
        this.osgiExplorer = osgiExplorer;
    }

    @SuppressWarnings("unchecked")
    public ClassSearchJob start(String phrase, List<String> bundleIds, List<List<String>> bundleClasses) {
        return start(prepareJob(phrase, bundleIds, bundleClasses));
    }

    private ClassSearchJob prepareJob(String phrase, List<String> bundleIds, List<List<String>> bundleClasses) {
        final ClassSearchJob job = new ClassSearchJob(osgiExplorer, phrase);

        for (String bundleId : bundleIds) {
            final Bundle bundle = osgiExplorer.findBundle(bundleId);
            if (bundle != null) {
                job.search(bundle);
            }
        }

        for (List<String> bundleClass : bundleClasses) {
            BundleClass clazz = osgiExplorer.findClass(bundleClass.get(0), bundleClass.get(1));
            if (clazz != null) {
                job.search(clazz);
            }

        }

        return job;
    }

}
