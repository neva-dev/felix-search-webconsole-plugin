package com.neva.felix.webconsole.plugins.search.core;

import com.neva.felix.webconsole.plugins.search.core.classsearch.ClassSearchJob;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchMonitor<T extends SearchJob> {

	private static final int JOB_CACHE_SIZE = 30;

	private final ExecutorService executor;

	private final Map<String, SearchJobDescriptor<T>> jobs = Collections.synchronizedMap(new LinkedHashMap<String, SearchJobDescriptor<T>>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, SearchJobDescriptor<T>> entry) {
			return size() > JOB_CACHE_SIZE;
		}
	});

	public SearchMonitor() {
		this.executor = Executors.newCachedThreadPool();
	}

	@SuppressWarnings("unchecked")
	public void start(T job) {
		final Future<T> future = (Future<T>) executor.submit(job);

		jobs.put(job.getId(), new SearchJobDescriptor<>(job, future));
	}

	public boolean stop(ClassSearchJob job) {
		SearchJobDescriptor<T> descriptor = jobs.get(job.getId());
		if (descriptor == null) {
			return false;
		}

		boolean canceled = descriptor.getFuture().cancel(true);
		jobs.remove(job.getId());

		return canceled;
	}

	public T get(String jobId) {
		final SearchJobDescriptor<T> descriptor = jobs.get(jobId);

		return descriptor != null ? descriptor.getJob() : null;
	}

	public void shutdown() {
		executor.shutdownNow();
	}
}
