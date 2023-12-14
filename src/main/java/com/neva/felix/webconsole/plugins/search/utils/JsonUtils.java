package com.neva.felix.webconsole.plugins.search.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Helper functions for JSON. Can be used for unifying servlet responses.
 */
public final class JsonUtils {

    public enum MessageType {
        SUCCESS,
        ERROR
    }

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
			.setPrettyPrinting()
            .create();

    private JsonUtils() {
        // hidden constructor
    }

	public static String toJson(Object obj) {
		return (obj instanceof String) ? (String) obj : GSON.toJson(obj);
	}

    public static void writeJson(HttpServletResponse response, final Object obj) throws IOException {
		String json = toJson(obj);

		writeJson(response, json);
    }

	public static void writeJson(HttpServletResponse response, String json)
            throws IOException {
        response.setContentType("application/json");
        response.setContentLength(json.getBytes("UTF-8").length);
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    public static void writeMessage(HttpServletResponse response, MessageType type, String text)
            throws IOException {
        writeMessage(response, type, text, Collections.<String, Object>emptyMap());
    }

    public static void writeMessage(HttpServletResponse response, MessageType type, String text, Object data)
            throws IOException {
        writeMessage(response, type, text, Collections.singletonMap("data", data));
    }

    public static void writeMessage(HttpServletResponse response, MessageType type, String text,
                                    Map<String, Object> context) throws IOException {
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put("type", type.name().toLowerCase())
                .put("message", text)
                .putAll(context).build();

        writeJson(response, props);
    }

}
