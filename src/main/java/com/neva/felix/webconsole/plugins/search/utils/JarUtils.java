package com.neva.felix.webconsole.plugins.search.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    public static void unzipClassInJar(String destinationDir, String jarPath, String classPath) throws IOException {
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
