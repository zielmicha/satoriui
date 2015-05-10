package net.atomshare.satori;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import spark.utils.IOUtils;

public class RSTRenderer {
	private static final Map<String, String> cache = new HashMap<>();

	public static String renderUncached(String data) {
		System.err.println("render RST " + data.length());
		ProcessBuilder builder = new ProcessBuilder(Arrays.asList("./rstproc"));
		builder.redirectInput(ProcessBuilder.Redirect.PIPE);
		builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
		try {
			Process process = builder.start();
			OutputStream stdin = process.getOutputStream();

			stdin.write(data.getBytes(Charset.forName("UTF-8")));
			stdin.close();
			return IOUtils.toString(process.getInputStream());
		} catch(IOException ex) {
			ex.printStackTrace();
			return "<div>Render error</div>";
		}
	}

	public static String render(String data) {
		synchronized (cache) {
			if(cache.containsKey(data))
				return cache.get(data);
		}
		String rendered = renderUncached(data);
		synchronized (cache) {
			cache.put(data, rendered);
		}
		return rendered;
	}
}

