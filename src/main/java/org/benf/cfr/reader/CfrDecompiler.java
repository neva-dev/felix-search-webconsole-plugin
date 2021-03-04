package org.benf.cfr.reader;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompiler;
import com.neva.felix.webconsole.plugins.search.utils.JarUtils;
import org.apache.commons.lang3.StringUtils;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.StringDumperFactoryImpl;

import java.io.File;
import java.io.IOException;

public class CfrDecompiler implements Decompiler {

    @Override
    public String decompile(File jar, String className, boolean showLineNumbers) throws IOException {
        File rootDir = Files.createTempDir();
        try {
            String path = StringUtils.replace(className, ".", "/");
            String packagePath = StringUtils.replace(StringUtils.substring(className, 0, StringUtils.lastIndexOf(className, ".")), ".", File.separator);
            String simpleClassName = StringUtils.substring(className, StringUtils.lastIndexOf(className, ".") + 1);
            File tmpdir = new File(rootDir.getAbsolutePath() + File.separator + packagePath);
            if (!tmpdir.mkdirs()) {
                throw new IOException("Couldn't create temporary directory: " + tmpdir);
            }
            JarUtils.unzipClassInJar(rootDir.getAbsolutePath(), jar.getAbsolutePath(), path);
            String pathToClass = tmpdir + File.separator + simpleClassName + ".class";
            Options options = OptionsImpl.getFactory().create(
                    ImmutableMap.<String, String>builder()
                            .put(OptionsImpl.JAR_FILTER.getName(), className)
                            .put(OptionsImpl.DECOMPILER_COMMENTS.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.DECOMPILE_INNER_CLASSES.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.SKIP_BATCH_INNER_CLASSES.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.ALLOW_CORRECTING.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.CASE_INSENSITIVE_FS_RENAME.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.COLLECTION_ITERATOR.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.ECLIPSE.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.FORBID_ANONYMOUS_CLASSES.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.FORBID_METHOD_SCOPED_CLASSES.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.HIDE_LANG_IMPORTS.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.HIDE_LONGSTRINGS.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.HIDE_UTF8.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.IGNORE_EXCEPTIONS.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.IGNORE_EXCEPTIONS_ALWAYS.getName(), Boolean.FALSE.toString())
                            .put(OptionsImpl.OVERRIDES.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.PREVIEW_FEATURES.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.REMOVE_BAD_GENERICS.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.REMOVE_BOILERPLATE.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.REMOVE_INNER_CLASS_SYNTHETICS.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.SUGAR_BOXING.getName(), Boolean.TRUE.toString())
                            .put(OptionsImpl.REWRITE_TRY_RESOURCES.getName(), Boolean.TRUE.toString())
                            .build());

            ClassFileSource2 classFileSource = new ClassFileSourceImpl(options);
            classFileSource.informAnalysisRelativePathDetail(null, null);
            // Note - both of these need to be reset, as they have caches.
            DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
            final StringDumperFactoryImpl stringDumperFactory = new StringDumperFactoryImpl(options);
            Driver.doClass(dcCommonState, pathToClass, false, stringDumperFactory);
            return stringDumperFactory.getTopLevelDump().toString();
        } finally {
            rootDir.delete();
        }

    }
}
