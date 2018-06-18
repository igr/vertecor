package com.oblac.vertecor;

import jodd.cli.Cli;

public class CmdLineParser {

	boolean noCache = false;
	boolean clearCache = false;
	String signature = null;
	String message = null;
	String hours = null;
	String date = null;

	public boolean isNoCache() {
		return noCache;
	}

	public boolean isClearCache() {
		return clearCache;
	}

	public String getSignature() {
		return signature;
	}

	public String getMessage() {
		return message;
	}

	public String getHours() {
		return hours;
	}

	public String getDate() {
		return date;
	}

	public CmdLineParser parse(String[] args) {
		Cli cli = new Cli();

		cli.option()
			.longName("nocache")
			.description("Disables the cache - nothing will be stored locally. Existing cache will not be used.")
			.with(v -> this.noCache = true);

		cli.option()
			.longName("clearcache")
			.description("Clears the cache before the usage.")
			.with(v -> this.noCache = true);

		cli.option()
			.shortName("d")
			.longName("date")
			.with(v -> this.date = v);

		cli.option()
			.shortName("m")
			.longName("message")
			.with(v -> this.message = v);

		cli.option()
			.shortName("h")
			.longName("hours")
			.with(v -> this.hours = v);

		cli.param()
			.label("IDS")
			.description("Project-phase-type IDs")
			.with(v -> this.signature = v[0]);

		cli.param()
			.label("HOURS")
			.description("Working hours")
			.with(v -> this.hours = v[0]);

		cli.param()
			.label("DESCRIPTION")
			.description("Description message")
			.with(v -> this.message = v[0]);

		cli.accept(args);

		return this;
	}
}
