package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.getopt.Options;

import java.io.BufferedOutputStream;

public class SimpleStringStreamDumper extends StreamDumper {
    private final StringBuilder stringBuilder;

    public SimpleStringStreamDumper(StringBuilder sb, TypeUsageInformation typeUsageInformation, Options options, IllegalIdentifierDump illegalIdentifierDump) {
        super(typeUsageInformation, options, illegalIdentifierDump, new MovableDumperContext());
        this.stringBuilder = sb;
}

    @Override
    protected void write(String s) {
        stringBuilder.append(s);
    }

    @Override
    public void close() {
    }

    @Override
    public void addSummaryError(Method method, String s) {
    }

    @Override
    public Dumper withTypeUsageInformation(TypeUsageInformation innerclassTypeUsageInformation) {
        throw new IllegalStateException();
    }

    @Override
    public BufferedOutputStream getAdditionalOutputStream(String description) {
        throw new IllegalStateException();
    }
}