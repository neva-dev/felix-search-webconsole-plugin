package com.neva.felix.webconsole.plugins.search.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchResult implements Serializable {

	// Serializables

	private final String id;

	private final String provider;

	private final String label;

	private String description;

	private Map<String, Object> context;

	// Internally used for post-processing

	private transient int rank;

	private transient List<String> phrases;

	public SearchResult(String provider, String id, int rank, String label) {
		this.provider = provider;
		this.rank = rank;
		this.label = label;
		this.context = Maps.newHashMap();

		this.id = id;
		this.phrases = Lists.newArrayList();
		this.phrases.add(label);
	}

	public SearchResult(String provider, String id, int rank, String label, String description) {
		this(provider, id, rank, label);
		this.description = description;
	}

	public SearchResult(String provider, String id, int rank, String label, String description, Map<String, Object> context) {
		this(provider, id, rank, label, description);
		this.context = context;
	}

	public String getProvider() {
		return provider;
	}

	public String getId() {
		return id;
	}

	public int getRank() {
		return rank;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	/**
	 * Get phrases which should be compared to queries phrase
	 */
	public List<String> getPhrases() {
		return ImmutableList.copyOf(phrases);
	}

	public SearchResult addPhrase(String phrase) {
		if (StringUtils.isNotBlank(phrase)) {
			phrases.add(phrase);
		}

		return this;
	}
}
