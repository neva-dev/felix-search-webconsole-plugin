package com.neva.felix.webconsole.plugins.search.decompiler.fernflower;

import com.google.common.io.Files;
import com.neva.felix.webconsole.plugins.search.decompiler.Decompiler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
            unzipClassInJar(rootDir.getAbsolutePath(), jar.getAbsolutePath(), path);
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

    private void unzipClassInJar(String destinationDir, String jarPath, String classPath) throws IOException {
        File file = new File(jarPath);
        JarFile jar = new JarFile(file);

        // fist get all directories,
        // then make those directory on the destination Path
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();

            final String entryName = entry.getName();
            String fileName = destinationDir + File.separator + entryName;
            File f = new File(fileName);

            if (fileName.endsWith("/") && classPath.contains(entryName)) {
                f.mkdirs();
            }
        }

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();

            final String entryName = entry.getName();
            String fileName = destinationDir + File.separator + entryName;
            File f = new File(fileName);

            if (!fileName.endsWith("/") && entryName.startsWith(classPath)) {
                try (InputStream is = jar.getInputStream(entry); FileOutputStream fos = new FileOutputStream(f)) {
                    // write contents of 'is' to 'fos'
                    while (is.available() > 0) {
                        fos.write(is.read());
                    }
                }
            }
        }
    }
}
