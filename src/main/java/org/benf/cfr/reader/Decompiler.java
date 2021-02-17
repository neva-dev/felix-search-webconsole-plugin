package org.benf.cfr.reader;

import com.google.common.collect.ImmutableMap;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.StringDumperFactoryImpl;

import java.io.File;

public class Decompiler {
    private final Options options;
    private final String path;

    public Decompiler(File jar, String clazzName) {
        path = jar.getAbsolutePath();
        options = OptionsImpl.getFactory().create(
                ImmutableMap.of(OptionsImpl.JAR_FILTER.getName(), clazzName));
    }

    public String decompile() {
        ClassFileSource2 classFileSource = new ClassFileSourceImpl(options);
        classFileSource.informAnalysisRelativePathDetail(null, null);
        // Note - both of these need to be reset, as they have caches.
        DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
        final StringDumperFactoryImpl stringDumperFactory = new StringDumperFactoryImpl(options);
        Driver.doJar(dcCommonState, path, AnalysisType.JAR, stringDumperFactory);
        return stringDumperFactory.getTopLevelDump().toString();
    }
}
