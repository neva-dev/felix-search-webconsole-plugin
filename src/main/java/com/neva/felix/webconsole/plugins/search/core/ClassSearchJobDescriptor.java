package com.neva.felix.webconsole.plugins.search.core;

import java.util.concurrent.Future;

public class ClassSearchJobDescriptor {

	private final ClassSearchJob job;

	private final Future<ClassSearchJob> future;

	public ClassSearchJobDescriptor(ClassSearchJob job, Future<ClassSearchJob> future) {
		this.job = job;
		this.future = future;
	}

	public ClassSearchJob getJob() {
		return job;
	}

	protected Future<ClassSearchJob> getFuture() {
		return future;
	}
}
