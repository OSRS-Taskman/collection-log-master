package com.collectionlogmaster.util;

import static com.collectionlogmaster.util.GsonOverride.GSON;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class FileUtils {
	public static <T> T loadResource(String resourcePath, Type clazz) {
		try (InputStream is = CollectionLogMasterPlugin.class.getResourceAsStream(resourcePath)) {
			assert is != null;
			return GSON.fromJson(new InputStreamReader(is), clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
