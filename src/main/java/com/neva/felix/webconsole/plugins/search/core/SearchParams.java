package com.neva.felix.webconsole.plugins.search.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class SearchParams {

	private static final int RESULT_LIMIT_DEFAULT = 60;

	private static final boolean CACHED_DEFAULT = true;

	private final String phrase;

	private final int resultLimit;

	private final List<String> providers;

	private final boolean cached;

	public SearchParams(String phrase, List<String> providers) {
		this(phrase, providers, RESULT_LIMIT_DEFAULT, CACHED_DEFAULT);
	}

	public SearchParams(String phrase, List<String> providers, Integer resultLimit, Boolean cached) {
		this.phrase = phrase;
		this.providers = FluentIterable.from(providers).filter(new Predicate<String>() {
			@Override
			public boolean apply(String s) {
				return StringUtils.isNotBlank(s);
			}
		}).toList();
		this.resultLimit = MoreObjects.firstNonNull(resultLimit, RESULT_LIMIT_DEFAULT);
		this.cached = MoreObjects.firstNonNull(cached, CACHED_DEFAULT);
	}

	public String getPhrase() {
		return phrase;
	}

	public int getResultLimit() {
		return resultLimit;
	}

	public List<String> getProviders() {
		return this.providers;
	}

	public boolean isCached() {
		return cached;
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(phrase);
		builder.append(resultLimit);
		builder.append(providers);

		return builder.toHashCode();
	}
}
