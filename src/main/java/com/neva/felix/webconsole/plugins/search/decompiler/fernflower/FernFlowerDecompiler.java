package com.neva.felix.webconsole.plugins.search.decompiler.fernflower;

import com.google.common.io.Files;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompiler;
import com.neva.felix.webconsole.plugins.search.utils.JarUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FernFlowerDecompiler implements Decompiler {
    @Override
    public String decompile(File jar, String className, boolean showLineNumbers) throws Exception {
        File rootDir = Files.createTempDir();
        try {
            String path = StringUtils.replace(className, ".", "/");
            String packagePath = StringUtils.replace(StringUtils.substring(className, 0, StringUtils.lastIndexOf(className, ".")), ".", File.separator);
            File tmpdir = new File(rootDir.getAbsolutePath() + File.separator + packagePath);
            if (!tmpdir.mkdirs()) {
                throw new IOException("Couldn't create temporary directory: " + tmpdir);
            }
            JarUtils.unzipClassInJar(rootDir.getAbsolutePath(), jar.getAbsolutePath(), path);
            final Map<String, Object> defaults = new HashMap<>(IFernflowerPreferences.DEFAULTS);
            defaults.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
            InMemoryDecompiler inMemoryDecompiler = new InMemoryDecompiler(rootDir, defaults, new Log4JLogger());
            inMemoryDecompiler.addLibrary(jar);
            inMemoryDecompiler.addSource(tmpdir);
            inMemoryDecompiler.decompileContext();
            return inMemoryDecompiler.getSource(path);
        } finally {
            rootDir.delete();
        }
    }
}
