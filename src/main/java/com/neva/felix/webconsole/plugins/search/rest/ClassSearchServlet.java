package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.classsearch.ClassSearchJob;
import com.neva.felix.webconsole.plugins.search.core.classsearch.ClassSearchMonitor;
import com.neva.felix.webconsole.plugins.search.core.OsgiExplorer;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

public class ClassSearchServlet extends RestServlet {

	private static final Logger LOG = LoggerFactory.getLogger(ClassSearchServlet.class);

	public static final String ALIAS_NAME = "class-search";

	private static final String PHRASE_PARAM = "phrase";

	private static final String JOB_ID_PARAM = "jobId";

	private static final String BUNDLE_ID_PROP = "bundleId[]";

	private static final String BUNDLE_CLASS_PROP = "bundleClass[]";

	private final ClassSearchMonitor monitor;

	public ClassSearchServlet(BundleContext bundleContext) {
		super(bundleContext);
		this.monitor = new ClassSearchMonitor(new OsgiExplorer(bundleContext));
	}

	@Override
	protected String getAliasName() {
		return ALIAS_NAME;
	}

	@Override
	public void destroy() {
		monitor.shutdown();
		super.destroy();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String phrase = getPhrase(request);
			ClassSearchJob job = monitor.start(phrase, getBundleIds(request), getBundleClasses(request));

			if (job == null) {
				writeMessage(response, MessageType.ERROR, String.format("Job cannot be started for phrase '%s'", phrase));
			} else {
				writeMessage(response, MessageType.SUCCESS, "Job started properly.", job);
			}
		} catch (Exception e) {
			LOG.error("Cannot search classes.", e);
			writeMessage(response, MessageType.ERROR, "Internal error occurred.");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String jobId = getJobId(request);
			ClassSearchJob job = monitor.get(jobId);

			if (job == null) {
				writeMessage(response, MessageType.ERROR, String.format("Job with ID '%s' is not running so it cannot be polled.", jobId));
			} else {
				job.poll();
				writeMessage(response, MessageType.SUCCESS, "Job polled properly.", job);
			}
		} catch (Exception e) {
			LOG.error("Cannot search classes.", e);
			writeMessage(response, MessageType.ERROR, "Internal error occurred.");
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String jobId = getJobId(request);
			ClassSearchJob job = monitor.get(jobId);

			if (job == null) {
				writeMessage(response, MessageType.ERROR, String.format("Job with ID '%s' is not running so it cannot be stopped.", jobId));
			} else {
				monitor.stop(job);
				writeMessage(response, MessageType.SUCCESS, "Job stopped properly.", job);
			}
		} catch (Exception e) {
			LOG.error("Cannot search classes.", e);
			writeMessage(response, MessageType.ERROR, "Internal error occurred.");
		}
	}

	private String getPhrase(HttpServletRequest request) {
		return StringUtils.trimToEmpty(request.getParameter(PHRASE_PARAM));
	}

	private String getJobId(HttpServletRequest request) {
		return StringUtils.trimToEmpty(request.getParameter(JOB_ID_PARAM));
	}

	private List<String> getBundleIds(HttpServletRequest request) {
		String[] values = request.getParameterValues(BUNDLE_ID_PROP);
		return ArrayUtils.isNotEmpty(values) ? Lists.newArrayList(values) : Collections.<String>emptyList();
	}

	private List<List<String>> getBundleClasses(HttpServletRequest request) {
		final String[] values = request.getParameterValues(BUNDLE_CLASS_PROP);
		if (ArrayUtils.isEmpty(values)) {
			return Collections.emptyList();
		}

		return FluentIterable.from(values).transform(new Function<String, List<String>>() {
			@Override
			public List<String> apply(String s) {
				return Splitter.on(",").omitEmptyStrings().trimResults().splitToList(s);
			}
		}).toList();
	}

}
