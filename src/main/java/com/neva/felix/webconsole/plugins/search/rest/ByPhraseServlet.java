package com.neva.felix.webconsole.plugins.search.rest;

import com.neva.felix.webconsole.plugins.search.core.SearchParams;
import com.neva.felix.webconsole.plugins.search.core.SearchProgress;
import com.neva.felix.webconsole.plugins.search.core.SearchService;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.MessageType;
import static com.neva.felix.webconsole.plugins.search.utils.JsonUtils.writeMessage;

public class ByPhraseServlet extends RestServlet {

	public static final String ALIAS_NAME = "by-phrase";

	public static final String PHRASE_PARAM = "phrase";

	public static final String PROVIDERS_PARAM = "provider";

	private static final String RESULT_LIMIT_PARAM = "resultLimit";

	private static final String CACHED_PARAM = "cached";

	private final SearchService search;

	public ByPhraseServlet(BundleContext bundleContext) {
		super(bundleContext);
		this.search = new SearchService(bundleContext);
	}

	@Override
	protected String getAliasName() {
		return ALIAS_NAME;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String phrase = getPhrase(request);
		if (phrase.isEmpty()) {
			writeMessage(response, MessageType.ERROR, "Phrase is not specified");
			return;
		}

		final List<String> providers = getProviders(request);
		if (providers.isEmpty()) {
			writeMessage(response, MessageType.ERROR, "None of providers are specified.");
			return;
		}

		final SearchParams params = new SearchParams(phrase, providers, getResultLimit(request), getCached(request));
		final SearchProgress progress = search.search(params);

		personalizeResult(response, params);
		writeMessage(response, MessageType.SUCCESS, "Results found", progress);
	}

	private String getPhrase(HttpServletRequest request) {
		return StringUtils.trimToEmpty(request.getParameter(PHRASE_PARAM));
	}

	private List<String> getProviders(HttpServletRequest request) {
		final String[] values = request.getParameterValues(PROVIDERS_PARAM);

		return values != null ? Lists.newArrayList(values) : Collections.<String>emptyList();
	}

	private Integer getResultLimit(HttpServletRequest request) {
		return Ints.tryParse(StringUtils.trimToEmpty(request.getParameter(RESULT_LIMIT_PARAM)));
	}

	private Boolean getCached(HttpServletRequest request) {
		return BooleanUtils.toBooleanObject(StringUtils.trimToEmpty(request.getParameter(CACHED_PARAM)));
	}

	private void personalizeResult(HttpServletResponse response, SearchParams params) {
		response.setHeader("Content-Disposition", String.format("attachment; filename='search_%s.json'", params.getPhrase()));
	}

	@Override
	public void destroy() {
		search.dispose();

		super.destroy();
	}
}
