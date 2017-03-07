package com.neva.felix.webconsole.plugins.search.core;

import java.util.concurrent.Future;

class SearchJobDescriptor<T extends SearchJob> {

    private final T job;

    private final Future<T> future;

    SearchJobDescriptor(T job, Future<T> future) {
        this.job = job;
        this.future = future;
    }

    T getJob() {
        return job;
    }

    Future<T> getFuture() {
        return future;
    }
}
