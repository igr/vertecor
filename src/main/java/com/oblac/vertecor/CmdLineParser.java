package com.oblac.vertecor;

public class CmdLineParser {

	boolean noCache = false;
	String signature = null;
	String message = null;
	String hours = null;
	String date = null;

	public boolean isNoCache() {
		return noCache;
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
		int argCount = 0;

		for (int i = 0, argsLength = args.length; i < argsLength; i++) {
			final String arg = args[i];

			// flags

			if (arg.equals("--nocache")) {
				noCache = true;
				continue;
			}

			// parameters

			if (arg.equals("-d") || arg.equals("--date")) {
				i++;
				date = args[i];
				continue;
			}

			if (arg.equals("-m") || arg.equals("--message")) {
				i++;
				message = args[i];
				continue;
			}

			if (arg.equals("-h") || arg.equals("--hours")) {
				i++;
				hours = args[i];
				continue;
			}

			// arguments

			if (argCount == 0) {
				signature = arg;
				argCount++;
				continue;
			}
			if (argCount == 1) {
				hours = arg;
				argCount++;
				continue;
			}
			if (argCount == 2) {
				message = arg;
				argCount++;
				continue;
			}
		}
		return this;
	}
}
