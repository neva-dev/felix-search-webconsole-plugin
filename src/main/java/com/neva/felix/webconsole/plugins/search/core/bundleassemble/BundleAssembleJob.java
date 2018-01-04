package com.neva.felix.webconsole.plugins.search.core.bundleassemble;

import com.neva.felix.webconsole.plugins.search.core.BundleJar;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.neva.felix.webconsole.plugins.search.core.SearchJob;
import com.neva.felix.webconsole.plugins.search.rest.FileDownloadServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAssembleJob extends SearchJob {

    private static final Logger LOG = LoggerFactory.getLogger(BundleAssembleJob.class);

    private static final String ZIP_FILE_PREFIX = "bundle-assemble_";

    private static final String ZIP_SUFFIX = ".zip";

    private static final String ZIP_NAME = "bundles.zip";

    private transient final Set<BundleJar> bundles;

    private String downloadUrl;

    public BundleAssembleJob(OsgiExplorer osgiExplorer, Set<BundleJar> bundles) {
        super(osgiExplorer);
        this.bundles = bundles;
        this.step = "Gathering";
    }

    @Override
    public void perform() {
        total = bundles.size();
        step = "Assembling";

        ZipOutputStream out = null;
        File zipFile = null;

        try {
            zipFile = File.createTempFile(ZIP_FILE_PREFIX, ZIP_SUFFIX);
            out = new ZipOutputStream(new FileOutputStream(zipFile));

            for (BundleJar bundleJar: bundles) {
                try {
                    final byte[] binary = IOUtils.toByteArray(new FileInputStream(bundleJar.getJar()));
                    final String name = osgiExplorer.proposeJarName(bundleJar.getBundle().getBundleId());
                    final ZipEntry entry = new ZipEntry(name);

                    out.putNextEntry(entry);
                    out.write(binary);
                    out.closeEntry();
                } catch (Exception e) {
                    LOG.warn("Cannot assemble bundle {}", bundleJar.getBundle().getSymbolicName());
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
