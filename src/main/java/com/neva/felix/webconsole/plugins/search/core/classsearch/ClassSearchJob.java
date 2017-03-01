package com.neva.felix.webconsole.plugins.search.core.classsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchJob;
import com.neva.felix.webconsole.plugins.search.core.SearchUtils;
import org.osgi.framework.Bundle;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClassSearchJob extends SearchJob {

    private transient final Set<BundleClass> classes = Sets.newLinkedHashSet();

    private transient final List<ClassSearchResult> results = Lists.newLinkedList();

    private final String phrase;

    private int contextLineCount = 5;

    private List<ClassSearchResult> partialResults = Collections.emptyList();

    public ClassSearchJob(OsgiExplorer osgiExplorer, String phrase) {
        super(osgiExplorer);
        this.phrase = phrase;
        this.step = "Gathering";
    }

    public ClassSearchJob search(Bundle bundle) {
        classes.addAll(Sets.newLinkedHashSet(osgiExplorer.findClasses(bundle)));
        return this;
    }

    public ClassSearchJob search(BundleClass clazz) {
        classes.add(clazz);
        return this;
    }

    public ClassSearchJob contextLineCount(int count) {
        this.contextLineCount = count;
        return this;
    }

    @Override
    public void perform() {
        total = classes.size();
        step = "Searching";

        for (BundleClass clazz : classes) {
            final String source = osgiExplorer.decompileClass(clazz);
            final List<String> contexts = SearchUtils.findContexts(phrase, source, contextLineCount);

            if (!contexts.isEmpty()) {
                synchronized (results) {
                    results.add(new ClassSearchResult(osgiExplorer.getContext(), clazz, contexts));
                }
            }

            progress = ((double) count / (double) classes.size()) * 100.0d;
            count++;
        }
    }

    public ClassSearchJob poll() {
        synchronized (results) {
            partialResults = Lists.newLinkedList(results);
            results.clear();
        }

        return this;
    }

    public String getPhrase() {
        return phrase;
    }

    public int getContextLineCount() {
        return contextLineCount;
    }

    public List<ClassSearchResult> getPartialResults() {
        return partialResults;
    }
}
