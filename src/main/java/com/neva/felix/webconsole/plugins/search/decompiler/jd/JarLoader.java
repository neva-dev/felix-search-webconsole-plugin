package com.neva.felix.webconsole.plugins.search.decompiler.jd;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarLoader implements Loader {
    private final JarFile _jarFile;

    public JarLoader(final JarFile jarFile) {
        _jarFile = jarFile;
    }

    @Override
    public boolean canLoad(String internalName) {
        return _jarFile != null && _jarFile.getJarEntry(internalName + ".class") != null;
    }

    @Override
    public byte[] load(String internalName) throws LoaderException {
        Buffer buffer = new Buffer();
        try {

            final JarEntry entry = _jarFile.getJarEntry(internalName + ".class");

            final InputStream inputStream = _jarFile.getInputStream(entry);

            int remainingBytes = inputStream.available();

            buffer.reset(remainingBytes);

            while (remainingBytes > 0) {
                final int bytesRead = inputStream.read(buffer.array(), buffer.position(), remainingBytes);

                if (bytesRead < 0) {
                    break;
                }

                buffer.position(buffer.position() + bytesRead);
                remainingBytes -= bytesRead;
            }

            buffer.position(0);

        } catch (IOException e) {
            throw new LoaderException(e);
        }
        return buffer.array();
    }
}
