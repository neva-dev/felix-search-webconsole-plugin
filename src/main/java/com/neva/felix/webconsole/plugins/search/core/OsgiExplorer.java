package com.neva.felix.webconsole.plugins.search.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

public class OsgiExplorer {

	public static final String BUNDLE_STORAGE_PROP = "org.osgi.framework.storage";

	public static final String BUNDLE_PATH_FORMAT = "%s/bundle%s";

	public static final String BUNDLE_JAR_FILE = "bundle.jar";

	public static final String JAR_EXT = "jar";

	private static final Logger LOG = LoggerFactory.getLogger(OsgiExplorer.class);

	private final MetaTypeService metaTypeService;

	private final ConfigurationAdmin configAdmin;

	private BundleContext context;

	public OsgiExplorer(BundleContext context) {
		this.context = context;
		this.metaTypeService = service(MetaTypeService.class);
		this.configAdmin = service(ConfigurationAdmin.class);
	}

	public File findDir(Long bundleId) {
		String bundleStorage = context.getProperty(BUNDLE_STORAGE_PROP);
		LOG.debug("bundle id: '{}', bundle storage: '{}'", bundleStorage);
		String bundlePath = String.format(BUNDLE_PATH_FORMAT, bundleStorage, bundleId);
		LOG.debug("bundle id: '{}', bundle path: '{}'", bundleId, bundlePath);
		return new File(bundlePath);
	}

	public File findJar(Long bundleId) {
		File bundleDir = findDir(bundleId);
		LOG.debug("bundle id: '{}', bundle dir: '{}'", bundleId, bundleDir);
		return findJar(bundleDir);
	}

	public File findJar(File bundleDir) {
		boolean bundleDirExists = false;

		try {
			bundleDirExists = bundleDir.exists();
		} catch (SecurityException se) {
			LOG.error("error while checking if file: '{}' exists", bundleDir, se);
		}

		try {
			if (bundleDirExists) {
				Collection<File> files = FileUtils.listFiles(bundleDir, new String[]{JAR_EXT}, true);
				LOG.debug("files inside '{}': {}", bundleDir, files);
				List<File> filteredFiles = FluentIterable.from(files).filter(new Predicate<File>() {
					@Override
					public boolean apply(File file) {
						return file.getName().equalsIgnoreCase(BUNDLE_JAR_FILE);
					}
				}).toSortedList(new Comparator<File>() {
					@Override
					public int compare(File f1, File f2) {
						return f2.getAbsolutePath().compareTo(f1.getAbsolutePath());
					}
				});

				return Iterables.getFirst(filteredFiles, null);
			} else {
				LOG.warn("bundle dir: '{}' does not exist", bundleDir);
			}
		} catch (SecurityException se) {
			LOG.error("error while getting absolute paths for files inside '{}'", bundleDir, se);
		}

		return null;
	}

	public String proposeJarName(String bundleId) {
		String result = BUNDLE_JAR_FILE;

		final Bundle bundle = context.getBundle(Long.valueOf(bundleId));
		if (bundle != null) {
			final String realName = FilenameUtils.getName(bundle.getLocation());
			if (StringUtils.isNotEmpty(realName) && JAR_EXT.equalsIgnoreCase(FilenameUtils.getExtension(realName))) {
				result = realName;
			} else {
				result = String.format("%s.%s", bundle.getSymbolicName(), JAR_EXT);
			}
		}

		return result;
	}

	public String decompileClass(BundleClass clazz) {
		return decompileClass(clazz.getBundle().getBundleId(), clazz.getClassName());
	}

	public String decompileClass(Long bundleId, String className) {
		File jarFile = findJar(bundleId);
		LOG.debug("jar file for bundle id: '{}': '{}'", bundleId, jarFile);
		return decompileClass(jarFile, className);
	}

	public String decompileClass(File jar, String className) {
		String source = StringUtils.EMPTY;
		String path = StringUtils.replace(className, ".", "/");

		try {
			try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
					DecompilerSettings settings = DecompilerSettings.javaDefaults();

					JarFile jarFile = new JarFile(jar);
					JarTypeLoader jarTypeLoader = new JarTypeLoader(jarFile);
					settings.setTypeLoader(jarTypeLoader);
					settings.setForceExplicitImports(true);
					settings.setForceExplicitTypeArguments(true);

					Decompiler.decompile(path, new PlainTextOutput(writer), settings);
					stream.flush();
				} catch (SecurityException se) {
					LOG.error("access to the file: '{}' is denied by security manager", jar, se);
				} catch (IOException ioe) {
					LOG.error("exception while creating JAR file object for '{}'", jar, ioe);
				}
				source = new String(stream.toByteArray(), Charset.defaultCharset());
			}
		} catch (IOException ioe){
			LOG.error("exception while closing ByteArrayOutputStream", ioe);
		}

		return source;
	}

	public Iterable<BundleClass> findClasses(final Bundle bundle) {
		return FluentIterable.from(new BundleScanner(bundle).findClassNames()).transform(new Function<String, BundleClass>() {
			@Override
			public BundleClass apply(String className) {
				return new BundleClass(bundle, className);
			}
		});
	}

	public Iterable<BundleClass> findClasses(String name) {
		if (StringUtils.isBlank(name)) {
			return Collections.emptyList();
		}

		final List<BundleClass> results = Lists.newArrayList();
		for (Bundle bundle : context.getBundles()) {
			final BundleClass clazz = findClass(bundle, name);
			if (clazz != null) {
				results.add(clazz);
			}
		}

		return results;
	}

	public BundleClass findClass(Configuration configuration) {
		BundleClass clazz = null;
		Bundle bundle = null;

		ServiceReference serviceReference = findServiceReference(configuration.getPid());
		if (serviceReference == null) {
			serviceReference = findServiceReference(configuration.getFactoryPid());
		}

		if (serviceReference != null) {
			bundle = serviceReference.getBundle();

			Object service = referenceService(serviceReference);
			if (service != null) {
				clazz = new BundleClass(bundle, service.getClass().getName());
			}
		} else {
			bundle = findBundle(configuration);
		}

		String classPid = StringUtils.defaultIfBlank(configuration.getFactoryPid(), configuration.getPid());
		if (clazz == null && bundle != null) {
			clazz = findClass(bundle, classPid);
		}

		if (clazz != null) {
			clazz = obtainClassMetadata(clazz, classPid);
		}

		return clazz;
	}

	private BundleClass obtainClassMetadata(BundleClass clazz, String classPid) {
		if (clazz != null) {
			ObjectClassDefinition definition = getClassDefinition(clazz.getBundle(), classPid);
			if (definition != null) {
				clazz = new MetaTypeBundleClass(clazz, definition);
			}
		}

		return clazz;
	}

	public Bundle findBundle(String id) {
		final Long longId = Longs.tryParse(id);

		return longId != null ? context.getBundle(longId) : null;
	}

	public Bundle findBundle(final Configuration configuration) {
		return FluentIterable.from(context.getBundles()).firstMatch(new Predicate<Bundle>() {
			@Override
			public boolean apply(Bundle bundle) {
				return StringUtils.equals(configuration.getBundleLocation(), bundle.getLocation());
			}
		}).orNull();
	}

	public BundleClass findClass(ServiceReference serviceReference) {
		BundleClass clazz = null;

		Bundle bundle = serviceReference.getBundle();
		String pid = (String) serviceReference.getProperty(Constants.SERVICE_PID);
		Object service = referenceService(serviceReference);

		if (service != null) {
			clazz = new BundleClass(bundle, service.getClass().getName());
		} else if (StringUtils.isNotBlank(pid)) {
			clazz = findClass(bundle, pid);
		}

		if (clazz != null) {
			clazz = obtainClassMetadata(clazz, pid);
		}

		return clazz;
	}

	public BundleClass findClass(String bundleId, String name) {
		final Bundle bundle = findBundle(bundleId);

		return bundle != null ? findClass(bundle, name) : null;
	}

	public BundleClass findClass(Bundle bundle, String name) {
		for (final String className : new BundleScanner(bundle).findClassNames(name)) {
			if (StringUtils.equals(className, name)) {
				return new BundleClass(bundle, name);
			}
		}

		return null;
	}

	public ObjectClassDefinition getClassDefinition(Bundle bundle, String pid) {
		ObjectClassDefinition result = null;

		if (bundle != null) {
			try {
				final MetaTypeInformation info = metaTypeService.getMetaTypeInformation(bundle);

				if (info != null) {
					result = info.getObjectClassDefinition(pid, null);
				}
			} catch (Throwable e) { //NOSONAR
				// ignore
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T service(Class<T> clazz) {
		ServiceReference reference = context.getServiceReference(clazz.getName());

		return reference != null ? (T) context.getService(reference) : null;
	}

	public Object findService(final String pid) {
		return referenceService(findServiceReference(pid));
	}

	public Object referenceService(ServiceReference reference) {
		Object service = null;
		if (reference != null) {
			try {
				service = context.getService(reference);
			} catch (NoClassDefFoundError e) { //NOSONAR
				// ignore classloading issues etc
			}
		}

		return service;
	}

	public ServiceReference findServiceReference(final String pid) {
		ServiceReference result = null;

		if (StringUtils.isNotBlank(pid)) {
			try {
				result = FluentIterable.from(context.getAllServiceReferences(null, null))
						.firstMatch(new Predicate<ServiceReference>() {
							@Override
							public boolean apply(ServiceReference serviceReference) {
								return StringUtils.equals(pid, (String) serviceReference.getProperty(Constants.SERVICE_PID));
							}
						}).orNull();
			} catch (InvalidSyntaxException e) { // NOSONAR
				// ignore
			}
		}

		return result;
	}

	public Iterable<ServiceReference> getServiceReferences() {
		try {
			return FluentIterable.from(context.getAllServiceReferences(null, null));
		} catch (InvalidSyntaxException e) {
			LOG.error("Cannot obtain references during service search.", e);
		}

		return Collections.emptyList();
	}

	public Iterable<Configuration> getConfigurations() {
		Iterable<Configuration> configurations = Collections.emptyList();

		if (configAdmin != null) {
			try {
				configurations = FluentIterable.from(configAdmin.listConfigurations(null));
			} catch (IOException | InvalidSyntaxException e) {
				LOG.error("Cannot read configurations", e);
			}
		}

		return configurations;
	}

	public BundleContext getContext() {
		return context;
	}
}
