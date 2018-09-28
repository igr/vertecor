package com.oblac.vertecor;

import com.oblac.vertecor.fx.App;
import com.oblac.vertecor.model.Phase;
import com.oblac.vertecor.model.Project;
import com.oblac.vertecor.model.ServiceType;
import com.oblac.vertecor.model.TimeEntry;
import com.oblac.vertecor.model.TimeEntryInput;
import com.oblac.vertecor.model.User;
import jodd.chalk.Chalk256;
import jodd.system.SystemUtil;
import jodd.util.function.Maybe;

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
		if (SystemUtil.info().isWindows()) {
			Chalk256.enabled = false;
		}

		CmdLineParser in = new CmdLineParser().parse(args);

		System.out.println();
		System.out.println(Chalk256.chalk().red().on("    VERTECOR v2.0.2"));
		System.out.println(Chalk256.chalk().gray().on("(coded with ❤  by igsp)"));
		System.out.println();

		// create components

		final VertecSession vertecSession = new VertecSession(in.isNoCache(), in.isClearCache());

		if (in.isUseUI()) {
			App.vertec = vertecSession;
			App.launch(App.class);
		}
		else {
			new Runner(vertecSession, in.getTimeEntryInput()).letsgo();
		}
	}


	// ---------------------------------------------------------------- letsgo

	final VertecSession vertec;
	final TimeEntryInput timeEntryInput;

	public Runner(VertecSession vertec, TimeEntryInput timeEntryInput) {
		this.vertec = vertec;
		this.timeEntryInput = timeEntryInput;
	}

	public void letsgo() {
		vertec.loadCachedVertecCredentials()
			.or(() -> Maybe.of(pickVertecCredentials()))
			.map(vertec::authAndLoadUser)
			.map(this::inputTimeEntry)
			.consumeJust(vertec::storeTimeEntry);
	}


	// ---------------------------------------------------------------- pickers

	private VertecCredentials pickVertecCredentials() {
		System.out.println("> Please, introduce yourself:");
		System.out.println(Chalk256.chalk().gray().on("(you have to do this only once)"));

		String username = readLine("username: ");
		String password = readLine("password: ");
		System.out.println();

		final VertecCredentials vc = new VertecCredentials();
		vc.setUsername(username);
		vc.setPassword(password);

		return vc;
	}

	private TimeEntry inputTimeEntry(User user) {
		final Project project = with(
			timeEntryInput.getProjectId(), vertec::loadProject,
			this::pickAProject);

		final Phase phase = with(
			timeEntryInput.getPhaseId(), vertec::loadProjectPhase,
			() -> pickAPhase(project));

		final ServiceType serviceType = with(
			timeEntryInput.getServiceTypeId(), vertec::loadServiceType,
			() -> pickAServiceType(project));

		if (timeEntryInput.getSignature() == null) {
			System.out.println();
			System.out.println(
				Chalk256.chalk().gray().on
					("TIP: you can pass this combination as: " + project.getId() + "," + phase.getId() + "," + serviceType.getId()));
		}

		final String description = with(
			timeEntryInput.getMessage(), m -> m,
			() -> readLine("> Now enter a description:\n")
		);

		final double time = with(
			timeEntryInput.getHours(), Double::parseDouble,
			() -> readDouble("> How many HOURS have you spent (you can enter decimals, too)?\n")
		);

		final String isoDate = timeEntryInput.getDate() == null ? LocalDate.now().toString() : timeEntryInput.getDate();

		final TimeEntry timeEntry = new TimeEntry();

		timeEntry
			.setUser(user)
			.setProject(project)
			.setPhase(phase)
			.setServiceType(serviceType)
			.setDescription(description)
			.setMinutes((int) (time * 60))
			.setDate(isoDate);

		return timeEntry;
	}

	private Project pickAProject() {
		System.out.println();
		System.out.println("> Your projects:");

		List<Project> projectList = vertec.loadAllProjects();

		for (int i = 0; i < projectList.size(); i++) {
			Project project = projectList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(project.getCode()));
			System.out.println(Chalk256.chalk().white().on(" - " + project.getDescription()));
		}

		int option = readOption("Select a project: ", projectList.size());
		return projectList.get(option - 1);
	}

	private Phase pickAPhase(Project project) {
		System.out.println();
		System.out.println("> Project phases:");

		List<Phase> phasesList = vertec.loadAllProjectPhases(project);

		for (int i = 0; i < phasesList.size(); i++) {
			Phase phase = phasesList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(phase.getCode()));
			System.out.println();
		}

		int option = readOption("Select a phase: ", phasesList.size());
		return phasesList.get(option - 1);
	}

	private ServiceType pickAServiceType(Project project) {
		System.out.println();
		System.out.println("> Service Types:");

		List<ServiceType> serviceTypeList = vertec.loadAllServiceTypes(project);

		for (int i = 0; i < serviceTypeList.size(); i++) {
			ServiceType serviceType = serviceTypeList.get(i);
			System.out.print(Chalk256.chalk().green().on("[" + (i + 1) + "] "));
			System.out.print(Chalk256.chalk().yellow().on(serviceType.getCode()));
			System.out.println();
		}

		int option = readOption("Select service type: ", serviceTypeList.size());
		return serviceTypeList.get(option - 1);
	}

	// ---------------------------------------------------------------- utils

	private <IN, OUT> OUT with(IN in, Function<IN, OUT> function, Supplier<OUT> supplier) {
		if (in != null) {
			return function.apply(in);
		}
		return supplier.get();
	}

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

	private double readDouble(final String message) {
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
