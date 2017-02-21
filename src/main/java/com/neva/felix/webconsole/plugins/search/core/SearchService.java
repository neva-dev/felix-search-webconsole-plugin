package com.neva.felix.webconsole.plugins.search.core;

import com.neva.felix.webconsole.plugins.search.core.provider.BundleSearchProvider;
import com.neva.felix.webconsole.plugins.search.core.provider.ClassSearchProvider;
import com.neva.felix.webconsole.plugins.search.core.provider.ConfigurationSearchProvider;
import com.neva.felix.webconsole.plugins.search.core.provider.ServiceSearchProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SearchService {

	private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

	private static final int PROVIDER_THREAD_POOL_SIZE_DEFAULT = 8;

	private static final int PHRASE_CACHE_SIZE_DEFAULT = 60;

	private static final int RANK_BOOST_DEFAULT = 1000;

	private static final int PROVIDER_TIMEOUT_DEFAULT = 30;

	private ExecutorService executor;

	private Map<Integer, SearchProgress> phraseCache;

	private int rankBoost;

	private int providerTimeout;

	private List<SearchProvider> providers;

	public SearchService(BundleContext bundleContext) {
		this(
				defaultProviders(bundleContext),
				PROVIDER_THREAD_POOL_SIZE_DEFAULT, PHRASE_CACHE_SIZE_DEFAULT, RANK_BOOST_DEFAULT, PROVIDER_TIMEOUT_DEFAULT
		);
	}

	public SearchService(List<SearchProvider> providers, int providerThreadPoolSize, final int phraseCacheSize, int rankBoost, int providerTimeout) {
		this.providers = providers;
		this.executor = Executors.newFixedThreadPool(providerThreadPoolSize);
		this.phraseCache = Collections.synchronizedMap(new LinkedHashMap<Integer, SearchProgress>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Integer, SearchProgress> entry) {
				return size() > phraseCacheSize;
			}
		});
		this.rankBoost = rankBoost;
		this.providerTimeout = providerTimeout;

		reset();
	}

	private static ImmutableList<SearchProvider> defaultProviders(BundleContext bundleContext) {
		return ImmutableList.<SearchProvider>of(
				new BundleSearchProvider(bundleContext),
				new ClassSearchProvider(bundleContext),
				new ServiceSearchProvider(bundleContext),
				new ConfigurationSearchProvider(bundleContext)
		);
	}

	public void dispose() {
		this.executor.shutdownNow();
	}

	public SearchProgress search(final SearchParams params) {
		final List<Callable<List<SearchResult>>> tasks = Lists.newArrayList();
		final List<SearchProvider> providers = findProviders(params);

		for (final SearchProvider provider : providers) {
			tasks.add(new Callable<List<SearchResult>>() {
				@Override
				public List<SearchResult> call() throws Exception {
					return provider.search(params);
				}
			});
		}

		return fetchResults(providers, params, tasks);
	}

	private List<SearchProvider> findProviders(SearchParams searchParams) {
		final ArrayList<SearchProvider> providers = Lists.newArrayList();
		for (SearchProvider provider : this.providers) {
			if (searchParams.getProviders().contains(provider.getLabel().toLowerCase())) {
				providers.add(provider);
			}
		}

		return providers;
	}

	private SearchProgress fetchResults(List<SearchProvider> providers, SearchParams params,
										List<Callable<List<SearchResult>>> tasks) {
		final int key = params.hashCode();
		final String text = params.getPhrase();

		SearchProgress progress;
		if (params.isCached() && phraseCache.containsKey(key)) {
			progress = phraseCache.get(key);
		} else {
			progress = new SearchProgress(params);
			progress.start();

			try {
				// Pool each provider in parallel
				final List<Future<List<SearchResult>>> futures = Lists.newArrayList();
				for (Callable<List<SearchResult>> task : tasks) {
					futures.add(executor.submit(task));
				}

				// Grab each response (or service time out)
				for (Future<List<SearchResult>> future : futures) {
					final SearchProvider provider = providers.get(futures.indexOf(future));
					final String providerName = provider != null ? provider.getLabel() : "<unknown>";

					try {
						progress.getResults().addAll(future.get(providerTimeout, TimeUnit.SECONDS));
					} catch (ExecutionException e) {
						final String message = String.format("%s execution error: %s", providerName,
								e.getMessage());

						LOG.error(message, e);
						progress.step(message);
					} catch (TimeoutException e) {
						final String message = String.format("%s exceeds time limit: %d second(s)",
								providerName, providerTimeout);

						LOG.error(message);
						progress.step(message);
					}
				}
			} catch (InterruptedException e) {
				final String message = "Cannot search by phrase (probably lack of resources)";

				LOG.error(message, e);
				progress.step(message);
			}

			progress.groupResultsById(text, rankBoost);
			progress.orderResultsByRank(text, rankBoost);
			progress.adjustResultsQuantity(params.getResultLimit());

			progress.stop();

			phraseCache.put(key, progress);
		}

		return progress;
	}

	public void reset() {
		phraseCache.clear();
		for (SearchProvider provider : providers) {
			provider.reset();
		}
	}

	public List<SearchProvider> getProviders() {
		return providers;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProvider(Class<T> providerClass) {
		for (SearchProvider provider : providers) {
			if (provider.getClass().equals(providerClass)) {
				return (T) provider;
			}
		}

		return null;
	}
}
