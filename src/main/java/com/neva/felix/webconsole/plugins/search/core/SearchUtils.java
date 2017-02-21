package com.neva.felix.webconsole.plugins.search.core;

import com.neva.felix.webconsole.plugins.search.utils.PrettifierUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchUtils {

	private static final String WORD_DELIMITER = " ";

	private static final int PHRASE_MIN_WORD_LENGTH_DEFAULT = 3;

	public static final String LINE_DELIMITER = "\n";

	private SearchUtils() {
		// cannot be constructed
	}

	/**
	 * Determine whether specified result ranking should be boosted
	 */
	public static boolean isResultRankBoosted(SearchResult result, String text) {
		for (String phrase : result.getPhrases()) {
			final String p = StringUtils.trimToEmpty(phrase).toLowerCase();
			final String t = StringUtils.trimToEmpty(text).toLowerCase();

			if (!p.isEmpty() && !t.isEmpty() && (p.startsWith(t) || p.endsWith(t))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculate search result score basing on input phrase (comparator function)
	 */
	public static int compareToPhrase(SearchResult sr1, SearchResult sr2, String text, int rankBoost) {
		final String phrase = StringUtils.trimToEmpty(text.toLowerCase());

		int r1 = sr1.getRank();
		int r2 = sr2.getRank();

		// Rank boost if one of phrases starts or ends with
		if (isResultRankBoosted(sr1, text)) {
			r1 -= rankBoost;
		}
		if (isResultRankBoosted(sr2, text)) {
			r2 -= rankBoost;
		}

		// Differences in rankings
		if (r1 != r2) {
			return r1 - r2;
		}

		// Substrings detection
		final String s1 = sr1.getLabel().toLowerCase();
		final String s2 = sr2.getLabel().toLowerCase();

		if ((s1.contains(text) && s2.contains(text)) || (!s1.contains(text) && !s2.contains(text))) {
			final int d1 = StringUtils.getLevenshteinDistance(phrase, s1);
			final int d2 = StringUtils.getLevenshteinDistance(phrase, s2);

			return d1 - d2;
		}

		if (s1.contains(text)) {
			return -1;
		}

		return 1;
	}

	/**
	 * Check whether phrase contains specified text (with multiple word support)
	 */
	public static boolean containsPhrase(final String phrase, final String text, int minWordLength) {
		if (FilenameUtils.wildcardMatch(text, phrase, IOCase.INSENSITIVE)) {
			return true;
		}

		for (String phraseWord : splitWords(phrase)) {
			for (String textWord : splitWords(text)) {
				final String p = StringUtils.trimToEmpty(phraseWord);
				final String t = StringUtils.trimToEmpty(textWord);

				if ((!p.isEmpty() && !t.isEmpty()) && (t.length() >= minWordLength) && (p.length()
						>= minWordLength) && (p.contains(t) || t.contains(p))) {
					return true;
				}
			}
		}

		return false;
	}

	private static String[] splitWords(String phrase) {
		return StringUtils.trimToEmpty(phrase).toLowerCase().split(WORD_DELIMITER);
	}

	/**
	 * Check whether phrase contains one of specified texts (with multiple word support)
	 */
	public static boolean containsPhrase(final String phrase, List<String> texts, int minWordLength) {
		for (String text : texts) {
			if (containsPhrase(phrase, text, minWordLength)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsPhrase(final String phrase, List<String> texts) {
		return containsPhrase(phrase, texts, PHRASE_MIN_WORD_LENGTH_DEFAULT);
	}

	public static boolean containsPhrase(final String phrase, String text) {
		return containsPhrase(phrase, Collections.singletonList(text));
	}

	/**
	 * Compose human readable parameter list as description
	 */
	public static String composeDescription(Map<String, Object> params) {
		List<String> lines = Lists.newArrayList();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String[]) {
				value = "[" + StringUtils.join((String[] )value, ", ") + "]";
			}

			lines.add(String.format("%s: %s", entry.getKey(), value));
		}

		return StringUtils.join(lines, "\n");
	}

	public static List<String> findContexts(String phrase, String source, int contextLineCount) {
		List<String> contexts = Lists.newLinkedList();
		List<String> lines = Splitter.on(LINE_DELIMITER).splitToList(source);

		int i = 0;
		for (String line : lines) {
			if (StringUtils.containsIgnoreCase(line, phrase)) {
				String before = Joiner.on("\n")
						.join(lines.subList(Math.max(0, i - contextLineCount - 1), Math.max(0, i - 1)));
				String after = Joiner.on("\n").join(lines.subList(Math.min(i + 1, lines.size()),
						Math.min(lines.size(), i + 1 + contextLineCount)));
				String context = PrettifierUtils.escape(before) + "\n" + PrettifierUtils
						.highlight(PrettifierUtils.escape(line)) + "\n" + PrettifierUtils.escape(after);

				contexts.add(context);
			}

			i++;
		}

		return contexts;
	}
}
