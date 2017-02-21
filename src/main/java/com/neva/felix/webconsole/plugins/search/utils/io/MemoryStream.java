package com.neva.felix.webconsole.plugins.search.utils.io;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Decorator for input stream which provides cloning. Also ensures that input is read only once.
 */
public class MemoryStream {

	private final InputStream input;

	private ByteArrayOutputStream output;

	public MemoryStream(InputStream input) {
		this.input = input;
	}

	public ByteArrayOutputStream readInput() throws IOException {
		if (output == null) {
			output = readInput(input);
		}

		return output;
	}

	private ByteArrayOutputStream readInput(InputStream input) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(input, out);
		out.flush();

		return out;
	}

	public void writeOutput(OutputStream out) throws IOException {
		InputStream in = cloneInput();
		IOUtils.copy(in, out);
		in.close();
		out.flush();
	}

	public ByteArrayInputStream cloneInput() throws IOException {
		return new ByteArrayInputStream(readInput().toByteArray());
	}

	public int getLength() throws IOException {
		return readInput().size();
	}

}
