package com.oblac.vertecor;

import com.oblac.vertecor.model.TimeEntryInput;
import jodd.cli.Cli;

public class CmdLineParser {

	final TimeEntryInput timeEntryInput = new TimeEntryInput();
	boolean noCache = false;
	boolean clearCache = false;
	boolean useUI;

	public boolean isNoCache() {
		return noCache;
	}

	public boolean isClearCache() {
		return clearCache;
	}

	public boolean isUseUI() {
		return useUI;
	}

	public TimeEntryInput getTimeEntryInput() {
		return timeEntryInput;
	}

	public CmdLineParser parse(String[] args) {
		Cli cli = new Cli();


		cli.option()
			.longName("ui")
			.description("Use UI because why not")
			.with(v -> useUI = true);

		cli.option()
			.longName("nocache")
			.description("Disables the cache - nothing will be stored locally. Existing cache will not be used.")
			.with(v -> noCache = true);

		cli.option()
			.longName("clearcache")
			.description("Clears the cache before the usage.")
			.with(v -> noCache = true);

		cli.option()
			.shortName("d")
			.longName("date")
			.with(timeEntryInput::setDate);

		cli.option()
			.shortName("m")
			.longName("message")
			.with(timeEntryInput::setMessage);

		cli.option()
			.shortName("h")
			.longName("hours")
			.with(timeEntryInput::setHours);

		cli.param()
			.label("IDS")
			.description("Project-phase-type IDs")
			.with(v -> timeEntryInput.setSignature(v[0]));

		cli.param()
			.label("HOURS")
			.description("Working hours")
			.with(v -> timeEntryInput.setHours(v[0]));

		cli.param()
			.label("DESCRIPTION")
			.description("Description message")
			.with(v -> timeEntryInput.setMessage(v[0]));

		cli.accept(args);

		return this;
	}
}
