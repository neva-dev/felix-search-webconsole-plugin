package com.neva.felix.webconsole.plugins.search.core.sourcegenerator;

import com.google.common.base.Charsets;
import com.neva.felix.webconsole.plugins.search.core.BundleClass;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchJob;
import com.neva.felix.webconsole.plugins.search.rest.FileDownloadServlet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SourceGeneratorJob extends SearchJob {

    private static final Logger LOG = LoggerFactory.getLogger(SourceGeneratorJob.class);

    private static final String ZIP_FILE_PREFIX = "source-generate_";

    private static final String ZIP_SUFFIX = ".zip";

    private static final String JAVA_SUFFIX = ".java";

    private static final String ZIP_NAME = "sources.zip";

    private transient final Set<BundleClass> classes;

    private String downloadUrl;

    public SourceGeneratorJob(OsgiExplorer osgiExplorer, Set<BundleClass> classes) {
        super(osgiExplorer);
        this.classes = classes;
        this.step = "Gathering";
    }

    @Override
    public void perform() {
        total = classes.size();
        step = "Generating";

        ZipOutputStream out = null;
        File zipFile = null;

        try {
            zipFile = File.createTempFile(ZIP_FILE_PREFIX, ZIP_SUFFIX);
            out = new ZipOutputStream(new FileOutputStream(zipFile));

            for (BundleClass clazz : classes) {
                try {
                    final byte[] source = osgiExplorer.decompileClass(clazz).getBytes(Charsets.UTF_8);
                    final String path = clazz.getBundle().getSymbolicName() + "/" + clazz.getClassPath() + JAVA_SUFFIX;
                    final ZipEntry entry = new ZipEntry(path);

                    out.putNextEntry(entry);
                    out.write(source);
                    out.closeEntry();
                } catch (Exception e) {
                    LOG.warn("Cannot generate sources for class {} form bundle {}", clazz.getClassName(),
                            clazz.getBundle().getBundleId());
                }

                increment();
            }
        } catch (IOException e) {
            LOG.error("IO error related with ZIP file in temporary directory.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }

        if (zipFile != null) {
            downloadUrl = FileDownloadServlet.url(osgiExplorer.getContext(), zipFile.getAbsolutePath(), ZIP_NAME);
            step = "Done";
        } else {
            step = "Ended with error";
        }
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
