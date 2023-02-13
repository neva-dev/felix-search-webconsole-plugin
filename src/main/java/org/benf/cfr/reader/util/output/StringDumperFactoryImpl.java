package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;

public class StringDumperFactoryImpl implements DumperFactory {
    private final Options options;
    private final ProgressDumper progressDumper;
    private final StringBuilder topLevelDump;

    public StringDumperFactoryImpl(Options options) {
        this.topLevelDump = new StringBuilder();
        this.options = options;
        progressDumper = ProgressDumperNop.INSTANCE;
    }

    @Override
    public DumperFactory getFactoryWithPrefix(String prefix, int version) {
        return this;
    }

    public Dumper getNewTopLevelDumper(JavaTypeInstance classType, SummaryDumper summaryDumper, TypeUsageInformation typeUsageInformation, IllegalIdentifierDump illegalIdentifierDump) {
        return new SimpleStringStreamDumper(topLevelDump, typeUsageInformation, options, illegalIdentifierDump);
    }

    private static class BytecodeDumpConsumerImpl implements BytecodeDumpConsumer {
        private final Dumper dumper;

        BytecodeDumpConsumerImpl(Dumper dumper) {
            this.dumper = dumper;
        }

        @Override
        public void accept(Collection<Item> items) {
            try {
                BufferedOutputStream stream = dumper.getAdditionalOutputStream("lineNumberTable");
                try (OutputStreamWriter sw = new OutputStreamWriter(stream)) {
                    sw.write("------------------\n");
                    sw.write("Line number table:\n\n");
                    for (Item item : items) {
                        sw.write(item.getMethod().getMethodPrototype().toString());
                        sw.write("\n----------\n");
                        for (Map.Entry<Integer, Integer> entry : item.getBytecodeLocs().entrySet()) {
                            sw.write("Line " + entry.getValue() + "\t: " + entry.getKey() + "\n");
                        }
                        sw.write("\n");
                    }
                }
            } catch (IOException e) {
                throw new ConfusedCFRException(e);
            }
        }
    }

    @Override
    public Dumper wrapLineNoDumper(Dumper dumper) {
        // There's really not a reason to do this, but it's useful for testing.
        if (options.getOption(OptionsImpl.TRACK_BYTECODE_LOC)) {
            return new BytecodeTrackingDumper(dumper, new BytecodeDumpConsumerImpl(dumper));
        }
        return dumper;
    }

    @Override
    public ExceptionDumper getExceptionDumper() {
        return new StdErrExceptionDumper();
    }

    /*
     * A summary dumper will receive errors.  Generally, it's only of value when dumping jars to file.
     */
    public SummaryDumper getSummaryDumper() {
        return new NopSummaryDumper();
    }

    @Override
    public ProgressDumper getProgressDumper() {
        return progressDumper;
    }

    public StringBuilder getTopLevelDump() {
        return topLevelDump;
    }
}
