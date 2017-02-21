package com.neva.felix.webconsole.plugins.search.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Fetched search progress results
 */
public class SearchProgress {

	private SearchParams params;

	private Date start;

	private Date stop;

	private long duration;

	private List<Object> steps = Lists.newArrayList();

	private List<SearchResult> results = Lists.newArrayList();

	public SearchProgress(SearchParams params) {
		this.params = params;
	}

	public void start() {
		this.start = new Date();
	}

	public void stop() {
		this.stop = new Date();
		this.duration = stop.getTime() - start.getTime();
	}

	public SearchParams getParams() {
		return params;
	}

	public Date getStart() {
		return start;
	}

	public Date getStop() {
		return stop;
	}

	public long getDuration() {
		return duration;
	}

	public void step(String message) {
		steps.add(message);
	}

	public List<Object> getSteps() {
		return steps;
	}

	public List<SearchResult> getResults() {
		return results;
	}

	/**
	 * Order results by its ranking (if equals use result which better matches phrase)
	 */
	public void orderResultsByRank(final String phrase, final int rankBoost) {
		Collections.sort(results, new Comparator<SearchResult>() {
			@Override
			public int compare(SearchResult s1, SearchResult s2) {
				return SearchUtils.compareToPhrase(s1, s2, phrase, rankBoost);
			}
		});
	}

	/**
	 * Removes duplicated ID's in search results, leaves results with best scores
	 */
	public void groupResultsById(String text, int rankBoost) {
		final Map<String, SearchResult> groupedResults = Maps.newHashMap();

		for (SearchResult result : results) {
			if (!groupedResults.containsKey(result.getId())) {
				groupedResults.put(result.getId(), result);
			} else {
				final SearchResult best = groupedResults.get(result.getId());

				if (SearchUtils.compareToPhrase(result, best, text, rankBoost) < 0) {
					groupedResults.put(result.getId(), result);
				}
			}
		}

		results.clear();
		results.addAll(groupedResults.values());
	}

	/**
	 * Ensure that we have not too many results
	 */
	public void adjustResultsQuantity(int maxSize) {
		if (results.size() > maxSize) {
			results = results.subList(0, maxSize);
		}
	}
}
