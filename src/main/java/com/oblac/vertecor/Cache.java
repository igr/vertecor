package com.oblac.vertecor;

import jodd.io.FileUtil;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.system.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * Simple JSON cache.
 */
public class Cache {

	private final File cacheFolder;
	private final boolean disabled;

	public Cache(final boolean noCache, final boolean clearCache) {
		this.disabled = noCache;
		this.cacheFolder = new File(SystemUtil.info().getHomeDir(), ".vertec");
		this.cacheFolder.mkdir();

		if (clearCache) {
			clear();
		}
	}

	/**
	 * Clears the cache, what else.
	 */
	public synchronized void clear() {
		try {
			FileUtil.deleteDir(cacheFolder);
		} catch (IOException ioex) {
			throw new UncheckedIOException(ioex);
		}
		this.cacheFolder.mkdir();
	}

	public synchronized <T> T fromCache(Object id, Class<T> type, Supplier<T> valueSupplier) {
		T t = loadFromCache(id, type);
		if (t == null) {
			t = valueSupplier.get();
			if (t != null) {
				storeToCache(id, t);
			}
		}
		return t;
	}

	private <T> T loadFromCache(Object id, Class<T> type) {
		if (disabled) {
			return null;
		}
		File cacheFile = new File(cacheFolder, id.toString() + ".json");

		if (!cacheFile.exists()) {
			return null;
		}

		try {
			String json = FileUtil.readString(cacheFile);

			return JsonParser.create().parse(json, type);
		} catch (IOException ioex) {
			throw new UncheckedIOException(ioex);
		}
	}

	private void storeToCache(Object id, Object object) {
		if (disabled) {
			return;
		}
		File cacheFile = new File(cacheFolder, id.toString() + ".json");

		String json = JsonSerializer.create().deep(true).serialize(object);

		try {
			FileUtil.writeString(cacheFile, json);
		} catch (IOException ioex) {
			throw new UncheckedIOException(ioex);
		}
	}

}
