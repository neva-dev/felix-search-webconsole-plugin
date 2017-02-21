package com.neva.felix.webconsole.plugins.search.core;

import java.util.List;

public interface SearchProvider {

	String getLabel();

	List<SearchResult> search(SearchParams searchParams);

	void reset();
}
