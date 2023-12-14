package com.neva.felix.webconsole.plugins.search.utils.io;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

public final class FileDownloader {

	private final HttpServletResponse response;

	private final MemoryStream fileContent;

	private final String fileName;

	public FileDownloader(HttpServletResponse response, InputStream input, String fileName) {
		this.response = response;
		this.fileContent = new MemoryStream(input);
		this.fileName = fileName;
	}

	public void download() throws IOException {
		setResponseHeaders();
		writeResponseOutput();
	}

	private void setResponseHeaders() throws IOException {
		response.setContentLength(fileContent.getLength());
		response.setContentType("application/force-download");
		response.setHeader("Content-Transfer-Encoding", "binary");
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=\"%s\"", fileName.trim()));
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
	}

	private void writeResponseOutput() throws IOException {
		fileContent.writeOutput(response.getOutputStream());
	}
}
