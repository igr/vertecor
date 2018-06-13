package com.oblac.vertecor;

import com.oblac.vertecor.model.Phase;
import com.oblac.vertecor.model.Project;
import com.oblac.vertecor.model.ServiceType;
import com.oblac.vertecor.model.TimeEntry;
import com.oblac.vertecor.model.User;
import jodd.util.Chalk256;
import jodd.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Runner {

	public static void main(String[] args) {
		CmdLineParser in = new CmdLineParser().parse(args);

		new Runner().letsgo(
			in.getSignature(),
			in.getHours(),
			in.getMessage(),
			in.getDate(),
			in.isNoCache());
	}

	private void letsgo(
			final String signature,
			final String hours,
			final String message,
			final String date,
			final boolean noCache) {

		System.out.println();
		System.out.println(Chalk256.chalk().red().on("    VERTECOR v1.1"));
		System.out.println(Chalk256.chalk().gray().on("(coded with ❤  by igsp)"));
		System.out.println();

		final VertecXml vertecXml = new VertecXml(noCache);

		final VertecCredentials vs = pickVertecCredentials(vertecXml);

		if (!vertecXml.authenticateUser(vs)) {
			System.err.println("Invalid credentials. More luck next time!");
			return;
		}

		final User user = vertecXml.loadUser();

		System.out.println("Hello, " + Chalk256.chalk().green().on(user.getFullName()) + " :)");

		// parse the signature

		Integer projectId = null;
		Integer phaseId = null;
		Integer serviceTypeId = null;

		if (signature != null) {
			String[] split = StringUtil.splitc(signature, ',');
			if (split.length >= 1) {
				projectId = Integer.parseInt(split[0].trim());
			}
			if (split.length >= 2) {
				phaseId = Integer.parseInt(split[1].trim());
			}
			if (split.length >= 3) {
				serviceTypeId = Integer.parseInt(split[2].trim());
			}
		}

		// continue with picking

		final Project project = with(
			projectId, vertecXml::loadProject,
			() -> pickAProject(vertecXml, user));

		final Phase phase = with(
			phaseId, vertecXml::loadPhase,
			() -> pickAPhase(vertecXml, project));

		final ServiceType serviceType = with(
			serviceTypeId, vertecXml::loadServiceType,
			() -> pickAServiceType(vertecXml, project));

		if (signature == null) {
			System.out.println();
			System.out.println(Chalk256.chalk().gray().on("TIP: you can pass this combination as: " + project.getId() + "," + phase.getId() + "," + serviceType.getId()));
		}

		final String description = with(
			message, m -> m,
			() -> readLine("> Now enter a description:\n")
		);

		final double time = with(
			hours, Double::parseDouble,
			() -> readDouble("> How many HOURS have you spent (you can enter decimals, too)?\n")
		);

		final String isoDate = date == null ? LocalDate.now().toString() : date;

		TimeEntry timeEntry = new TimeEntry();

		timeEntry
			.setProject(project)
			.setPhase(phase)
			.setServiceType(serviceType)
			.setDescription(description)
			.setMinutes((int) (time * 60))
			.setDate(isoDate);

		System.out.println();
		System.out.println(Chalk256.chalk().cyan().on("Lay back, the Vertec is being updated..."));

		boolean success = vertecXml.storeTimeEntry(user, timeEntry);

		if (success) {
			System.out.println(Chalk256.chalk().green().on("Done."));
			System.out.println();
		}
		else {
			System.out.println("Something went wrong!");
			System.out.println();
		}
	}

	// ---------------------------------------------------------------- pickers

	/**
	 * Picks username and password.
	 */
	private VertecCredentials pickVertecCredentials(VertecXml vertecXml) {
		VertecCredentials vc = vertecXml.credentialsFromCache(null);
		if (vc != null) {
			return vc;
		}

		System.out.println("> Please, introduce yourself:");
		System.out.println(Chalk256.chalk().gray().on("(you have to do this only once)"));

		String username = readLine("username: ");
		String password = readLine("password: ");
		System.out.println();

		vc = new VertecCredentials();
		vc.setUsername(username);
		vc.setPassword(password);

		vertecXml.credentialsFromCache(vc);

		return vc;
	}

	private Project pickAProject(VertecXml vertecXml, User user) {
		System.out.println();
		System.out.println("> Your projects:");

		List<Project> projectList = vertecXml.loadUserActiveProjects(user);

		for (int i = 0; i < projectList.size(); i++) {
			Project project = projectList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(project.getCode()));
			System.out.println(Chalk256.chalk().white().on(" - " + project.getDescription()));
		}

		int option = readOption("Select a project: ", projectList.size());
		return projectList.get(option - 1);
	}

	private Phase pickAPhase(VertecXml vertecXml, Project project) {
		System.out.println();
		System.out.println("> Project phases:");

		List<Phase> phasesList = vertecXml.loadProjectPhases(project);

		for (int i = 0; i < phasesList.size(); i++) {
			Phase phase = phasesList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(phase.getCode()));
			System.out.println();
		}

		int option = readOption("Select a phase: ", phasesList.size());
		return phasesList.get(option - 1);
	}

	private ServiceType pickAServiceType(VertecXml vertecXml, Project project) {
		System.out.println();
		System.out.println("> Service Types:");

		List<ServiceType> serviceTypeList = vertecXml.loadServiceTypes(project);

		for (int i = 0; i < serviceTypeList.size(); i++) {
			ServiceType serviceType = serviceTypeList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(serviceType.getCode()));
			System.out.println();
		}

		int option = readOption("Select service type: ", serviceTypeList.size());
		return serviceTypeList.get(option - 1);
	}

	// ---------------------------------------------------------------- with

	private <IN, OUT> OUT with(IN in, Function<IN, OUT> function, Supplier<OUT> supplier) {
		if (in != null) {
			return function.apply(in);
		}
		return supplier.get();
	}

	// ---------------------------------------------------------------- utils

	private String readLine(String message) {
		System.out.println();
		System.out.print(message);

		if (System.console() != null) {
			return System.console().readLine();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			return reader.readLine();
		} catch (IOException ioex) {
			throw new UncheckedIOException(ioex);
		}
	}

	private int readOption(String message, int max) {
		while (true) {
			String line = readLine(message);
			int option;
			try {
				option = Integer.parseInt(line.trim());
			} catch (NumberFormatException nfex) {
				option = 0;
			}

			if (option < 1 || option > max) {
				System.out.println("Input error, please try again.");
				continue;
			}
			return option;
		}
	}

	private double readDouble(String message) {
		while (true) {
			String line = readLine(message);
			double value;
			try {
				value = Double.parseDouble(line.trim());
			} catch (NumberFormatException nfex) {
				System.out.println("Input error, please try again.");
				continue;
			}
			return value;
		}
	}

}
