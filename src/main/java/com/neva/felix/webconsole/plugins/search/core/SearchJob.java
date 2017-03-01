package com.neva.felix.webconsole.plugins.search.core;

import java.io.Serializable;
import java.util.UUID;

public abstract class SearchJob implements Runnable, Serializable {

    protected final transient OsgiExplorer osgiExplorer;

    private final String id;

    protected double progress = 0d;

    protected int count;

    protected int total;

    protected String step = "Initializing";

    public SearchJob(OsgiExplorer osgiExplorer) {
        this.osgiExplorer = osgiExplorer;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    @Override
    public void run() {
        count = 0;
        perform();
        progress = 100;
    }

    protected abstract void perform();

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
}
