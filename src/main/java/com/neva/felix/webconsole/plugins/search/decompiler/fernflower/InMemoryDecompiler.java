package com.neva.felix.webconsole.plugins.search.decompiler.fernflower;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class InMemoryDecompiler implements IBytecodeProvider, IResultSaver {
    private final File root;
    private final Fernflower engine;
    private final Map<String, ZipOutputStream> mapArchiveStreams = new HashMap<>();
    private final Map<String, Set<String>> mapArchiveEntries = new HashMap<>();
    private Map<String, String> sourceMap;

    protected InMemoryDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
        root = destination;
        engine = new Fernflower(this, this, options, logger);
        sourceMap = new HashMap<>();
    }

    public void addSource(File source) {
        engine.addSource(source);
    }

    public void addLibrary(File library) {
        engine.addLibrary(library);
    }

    public void decompileContext() {
        try {
            engine.decompileContext();
        } finally {
            engine.clearContext();
        }
    }

    // *******************************************************************
    // Interface IBytecodeProvider
    // *******************************************************************

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        File file = new File(externalPath);
        if (internalPath == null) {
            return InterpreterUtil.getBytes(file);
        } else {
            try (ZipFile archive = new ZipFile(file)) {
                ZipEntry entry = archive.getEntry(internalPath);
                if (entry == null) throw new IOException("Entry not found: " + internalPath);
                return InterpreterUtil.getBytes(archive, entry);
            }
        }
    }

    // *******************************************************************
    // Interface IResultSaver
    // *******************************************************************

    private String getAbsolutePath(String path) {
        return new File(root, path).getAbsolutePath();
    }

    @Override
    public void saveFolder(String path) {
        File dir = new File(getAbsolutePath(path));
        if (!(dir.mkdirs() || dir.isDirectory())) {
            throw new RuntimeException("Cannot create directory " + dir);
        }
    }

    @Override
    public void copyFile(String source, String path, String entryName) {
        try {
            InterpreterUtil.copyFile(new File(source), new File(getAbsolutePath(path), entryName));
        } catch (IOException ex) {
            DecompilerContext.getLogger().writeMessage("Cannot copy " + source + " to " + entryName, ex);
        }
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        sourceMap.put(qualifiedName, content);
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {
        File file = new File(getAbsolutePath(path), archiveName);
        try {
            if (!(file.createNewFile() || file.isFile())) {
                throw new IOException("Cannot create file " + file);
            }

            FileOutputStream fileStream = new FileOutputStream(file);
            ZipOutputStream zipStream = manifest != null ? new JarOutputStream(fileStream, manifest) : new ZipOutputStream(fileStream);
            mapArchiveStreams.put(file.getPath(), zipStream);
        } catch (IOException ex) {
            DecompilerContext.getLogger().writeMessage("Cannot create archive " + file, ex);
        }
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {
        saveClassEntry(path, archiveName, null, entryName, null);
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entryName) {
        String file = new File(getAbsolutePath(path), archiveName).getPath();

        if (!checkEntry(entryName, file)) {
            return;
        }

        try (ZipFile srcArchive = new ZipFile(new File(source))) {
            ZipEntry entry = srcArchive.getEntry(entryName);
            if (entry != null) {
                try (InputStream in = srcArchive.getInputStream(entry)) {
                    ZipOutputStream out = mapArchiveStreams.get(file);
                    out.putNextEntry(new ZipEntry(entryName));
                    InterpreterUtil.copyStream(in, out);
                }
            }
        } catch (IOException ex) {
            String message = "Cannot copy entry " + entryName + " from " + source + " to " + file;
            DecompilerContext.getLogger().writeMessage(message, ex);
        }
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        String file = new File(getAbsolutePath(path), archiveName).getPath();

        if (!checkEntry(entryName, file)) {
            return;
        }

        try {
            ZipOutputStream out = mapArchiveStreams.get(file);
            out.putNextEntry(new ZipEntry(entryName));
            if (content != null) {
                sourceMap.put(qualifiedName,content);
                out.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            String message = "Cannot write entry " + entryName + " to " + file;
            DecompilerContext.getLogger().writeMessage(message, ex);
        }
    }

    private boolean checkEntry(String entryName, String file) {
        Set<String> set = mapArchiveEntries.computeIfAbsent(file, k -> new HashSet<>());

        boolean added = set.add(entryName);
        if (!added) {
            String message = "Zip entry " + entryName + " already exists in " + file;
            DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
        }
        return added;
    }

    @Override
    public void closeArchive(String path, String archiveName) {
        String file = new File(getAbsolutePath(path), archiveName).getPath();
        try {
            mapArchiveEntries.remove(file);
            mapArchiveStreams.remove(file).close();
        } catch (IOException ex) {
            DecompilerContext.getLogger().writeMessage("Cannot close " + file, IFernflowerLogger.Severity.WARN);
        }
    }

    public String getSource(String qualifiedClassName) {
        return sourceMap.get(qualifiedClassName);
    }
}
