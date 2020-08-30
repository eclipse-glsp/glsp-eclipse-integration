package org.eclipse.glsp.integration.editor.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class ResourceUtil {
	private static final ResourceUtil INSTANCE = new ResourceUtil();

	public static boolean copyFromResource(String resourcePath, File destFile) {
		final ClassLoader classLoader = INSTANCE.getClass().getClassLoader();
		try {

			final InputStream stream = classLoader.getResourceAsStream(resourcePath);
			if (stream == null) {
				return false;
			}
			FileUtils.copyInputStreamToFile(stream, destFile);
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	private ResourceUtil() {
	}

}