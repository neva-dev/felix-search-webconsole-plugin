package com.neva.felix.webconsole.plugins.search.utils;

import org.apache.commons.lang3.StringUtils;

public final class PrettifierUtils {

	private PrettifierUtils() {
		// hidden constructor
	}

	public static String escape(String source) {
		String[] search = { "<", ">", };
		String[] replace = { "&lt;", "&gt;" };

		return StringUtils.replaceEach(source, search, replace);
	}

	public static String highlight(String line) {
		return String.format("<span class=\"highlighted\">%s</span>", line);
	}

}
