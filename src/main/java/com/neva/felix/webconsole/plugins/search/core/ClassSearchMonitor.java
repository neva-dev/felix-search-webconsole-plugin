package com.neva.felix.webconsole.plugins.search.core;

import org.osgi.framework.Bundle;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClassSearchMonitor {

	private static final int JOB_CACHE_SIZE = 30;

	private final ExecutorService executor;

	private final Map<String, ClassSearchJobDescriptor> jobs = Collections.synchronizedMap(new LinkedHashMap<String, ClassSearchJobDescriptor>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, ClassSearchJobDescriptor> entry) {
			return size() > JOB_CACHE_SIZE;
		}
	});

	private final OsgiExplorer osgiExplorer;

	public ClassSearchMonitor(OsgiExplorer osgiExplorer) {
		this.osgiExplorer = osgiExplorer;
		this.executor = Executors.newSingleThreadExecutor();
	}


	@SuppressWarnings("unchecked")
	public ClassSearchJob start(String phrase, List<String> bundleIds, List<List<String>> bundleClasses) {
		final ClassSearchJob job = prepareJob(phrase, bundleIds, bundleClasses);
		final Future<ClassSearchJob> future = (Future<ClassSearchJob>) executor.submit(job);

		jobs.put(job.getId(), new ClassSearchJobDescriptor(job, future));

		return job;
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

	public boolean stop(ClassSearchJob job) {
		ClassSearchJobDescriptor descriptor = jobs.get(job.getId());
		if (descriptor == null) {
			return false;
		}

		boolean canceled = descriptor.getFuture().cancel(true);
		jobs.remove(job.getId());

		return canceled;
	}

	public ClassSearchJob get(String jobId) {
		final ClassSearchJobDescriptor descriptor = jobs.get(jobId);

		return descriptor != null ? descriptor.getJob() : null;
	}

	public void shutdown() {
		executor.shutdownNow();
	}
}
