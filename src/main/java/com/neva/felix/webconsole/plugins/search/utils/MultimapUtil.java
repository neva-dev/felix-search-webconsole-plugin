package com.neva.felix.webconsole.plugins.search.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Provides useful methods for operating on e.g deserialized JSON objects. Notice that all nested maps should
 * have equal key type - string.
 */
public final class MultimapUtil {

	private static final String PATH_SEPARATOR = "/";

	private MultimapUtil() {
		// hidden constructor
	}

	/**
	 * Combine two multi-level maps recursively.
	 */
	@SuppressWarnings("unchecked")
	public static void extend(final Map<String, Object> first, final Map<String, Object> second) {
		for (Map.Entry<String, Object> entry : second.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof Map) {
				if (!first.containsKey(key)) {
					first.put(key, Maps.<String, Object> newLinkedHashMap());
				}

				extend((Map<String, Object>) first.get(key), (Map<String, Object>) value);
			} else {
				first.put(key, value);
			}
		}
	}

	/**
	 * Get value from multi-level map.
	 * 
	 * @param path Keys sequence joined by '/' character)
	 */
	@SuppressWarnings("unchecked")
	public static Object get(final Map<String, Object> map, final String path) {
		String[] parts = path.split(PATH_SEPARATOR);

		Map<String, Object> current = map;
		for (int i = 0; i < parts.length; i++) {
			String key = parts[i];
			if (i + 1 < parts.length) {
				if (!current.containsKey(key)) {
					break;
				}
				current = (Map<String, Object>) current.get(key);
			} else {
				return current.get(key);
			}
		}

		return null;
	}

	/**
	 * Put value into multi-level map.
	 *
	 * @param path Keys sequence joined by '/' character)
	 */
	@SuppressWarnings("unchecked")
	public static void put(final Map<String, Object> map, final String path, Object value) {
		String[] parts = path.split(PATH_SEPARATOR);

		Map<String, Object> current = map;
		for (int i = 0; i < parts.length; i++) {
			String key = parts[i];
			if (i + 1 < parts.length) {
				if (!current.containsKey(key)) {
					current.put(key, Maps.<String, Object> newLinkedHashMap());
				}
				current = (Map<String, Object>) current.get(key);
			} else {
				current.put(key, value);
			}
		}
	}

	/**
	 * Remove value from nested multi-level map.
	 *
	 * @param path Keys sequence joined by '/' character)
	 */
	@SuppressWarnings("unchecked")
	public static boolean remove(final Map<String, Object> map, final String path) {
		String[] parts = path.split(PATH_SEPARATOR);

		Map<String, Object> current = map;
		for (int i = 0; i < parts.length; i++) {
			String key = parts[i];
			if (i + 1 < parts.length) {
				if (!current.containsKey(key)) {
					return false;
				}
				current = (Map<String, Object>) current.get(key);
			} else if (current.containsKey(key)) {
				current.remove(key);
			}
		}

		return true;
	}

	/**
	 * Copy value (or reference!) from one multi-level map to another.
	 *
	 * @param path Keys sequence joined by '/' character)
	 */
	public static void copy(Map<String, Object> source, Map<String, Object> target, String path) {
		put(target, path, get(source, path));
	}

	/**
	 * Move value (or reference!) from one multi-level map to another.
	 *
	 * @param path Keys sequence joined by '/' character)
	 */
	public static void move(Map<String, Object> source, Map<String, Object> target, String path) {
		copy(source, target, path);
		remove(source, path);
	}

	/**
	 * Find parent map by its child property value.
	 */
    @SuppressWarnings("unchecked")
	public static Map<String, Object> find(Map<String, Object> map, String property, Object value) {
		Map<String, Object> result = null;
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (map.containsKey(property) && map.get(property).equals(value)) {
				return map;
			}

			if (entry.getValue() instanceof Map) {
				result = find((Map<String, Object>) entry.getValue(), property, value);
			}
		}

		return result;
	}
}
