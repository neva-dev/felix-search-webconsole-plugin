package org.benf.cfr.reader;

import com.google.common.collect.ImmutableMap;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompiler;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.StringDumperFactoryImpl;

import java.io.File;

public class CfrDecompiler implements Decompiler {

    @Override
    public String decompile(File jar, String className, boolean showLineNumbers) {
        String path = jar.getAbsolutePath();
        Options options = OptionsImpl.getFactory().create(
                ImmutableMap.of(OptionsImpl.JAR_FILTER.getName(), className));
        ClassFileSource2 classFileSource = new ClassFileSourceImpl(options);
        classFileSource.informAnalysisRelativePathDetail(null, null);
        // Note - both of these need to be reset, as they have caches.
        DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
        final StringDumperFactoryImpl stringDumperFactory = new StringDumperFactoryImpl(options);
        Driver.doJar(dcCommonState, path, AnalysisType.JAR, stringDumperFactory);
        return stringDumperFactory.getTopLevelDump().toString();
    }
}
