package com.neva.felix.webconsole.plugins.search.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.osgi.framework.Bundle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ClassSearchJob implements Runnable, Serializable {

	private transient final OsgiExplorer osgiExplorer;

	private transient final Set<BundleClass> classes = Sets.newLinkedHashSet();

	private transient final List<ClassSearchResult> results = Lists.newLinkedList();

	private final String phrase;

	private final String id;

	private double progress = 0d;

	private int contextLineCount = 5;

	private List<ClassSearchResult> partialResults = Collections.emptyList();

	private int count;

	private int total;

	private String step = "Gathering";

	public ClassSearchJob(OsgiExplorer osgiExplorer, String phrase) {
		this.osgiExplorer = osgiExplorer;
		this.phrase = phrase;
		this.id = UUID.randomUUID().toString();
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
	public void run() {
		count = 0;
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

		progress = 100;
	}

	public ClassSearchJob poll() {
		synchronized (results) {
			partialResults = Lists.newLinkedList(results);
			results.clear();
		}

		return this;
	}

	public String getId() {
		return id;
	}

	public String getPhrase() {
		return phrase;
	}

	public double getProgress() {
		return progress;
	}

	public int getCount() {
		return count;
	}

	public int getTotal() {
		return total;
	}

	public String getStep() {
		return step;
	}

	public int getContextLineCount() {
		return contextLineCount;
	}

	public List<ClassSearchResult> getPartialResults() {
		return partialResults;
	}
}
