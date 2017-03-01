package com.neva.felix.webconsole.plugins.search.core.classsearch;

import com.google.common.collect.Lists;
import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchJob;
import com.neva.felix.webconsole.plugins.search.core.SearchUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClassSearchJob extends SearchJob {

    private transient final List<ClassSearchResult> results = Lists.newLinkedList();

    private transient final Set<BundleClass> classes;

    private final String phrase;

    private int contextLineCount = 5;

    private List<ClassSearchResult> partialResults = Collections.emptyList();

    public ClassSearchJob(OsgiExplorer osgiExplorer, String phrase, Set<BundleClass> classes) {
        super(osgiExplorer);
        this.phrase = phrase;
        this.classes = classes;

        this.step = "Gathering";
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

            increment();
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
