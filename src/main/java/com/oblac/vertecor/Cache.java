package com.oblac.vertecor;

import jodd.io.FileUtil;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.system.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

public class Cache {

	private final File cacheFolder;

	public Cache() {
		this.cacheFolder = new File(SystemUtil.info().getHomeDir(), ".vertec");
		this.cacheFolder.mkdir();
	}

	public synchronized void clean() {
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
		File cacheFile = new File(cacheFolder, id.toString() + ".json");

		String json = JsonSerializer.create().deep(true).serialize(object);

		try {
			FileUtil.writeString(cacheFile, json);
		} catch (IOException ioex) {
			throw new UncheckedIOException(ioex);
		}
	}

}
