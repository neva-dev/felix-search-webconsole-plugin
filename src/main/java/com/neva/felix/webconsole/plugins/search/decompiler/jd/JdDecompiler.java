package com.neva.felix.webconsole.plugins.search.decompiler.jd;

import com.neva.felix.webconsole.plugins.search.decompiler.Decompiler;
import com.neva.felix.webconsole.plugins.search.decompiler.JarLoader;
import org.apache.commons.lang3.StringUtils;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;

import java.io.File;
import java.util.jar.JarFile;

public class JdDecompiler implements Decompiler {
    @Override
    public String decompile(File jar, String className, boolean showLineNumbers) throws Exception {
        String path = StringUtils.replace(className, ".", "/");
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        Loader loader = new JarLoader(new JarFile(jar, true));
        StringPrinter printer = new StringPrinter();
        printer.setDisplayLineNumbers(showLineNumbers);
        decompiler.decompile(loader, printer, path);
        return printer.toString();
    }
}
