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

public class Runner {

	public static void main(String[] args) {
		boolean noCache = false;
		String signature = null;
		String message = null;
		String hours = null;

		for (String arg : args) {
			if (arg.equals("--nocache")) {
				noCache = true;
				continue;
			}

			if (signature == null) {
				signature = arg;
				continue;
			}
			if (hours == null) {
				hours = arg;
				continue;
			}
			if (message == null) {
				message = arg;
				continue;
			}
		}
		new Runner().letsgo(signature, hours, message, noCache);
	}

	private void letsgo(final String signature, final String hours, final String message, final boolean noCache) {
		System.out.println();
		System.out.println(Chalk256.chalk().red().on("   VERTECOR v1.0"));
		System.out.println("Save your Vertec time!");
		System.out.println(Chalk256.chalk().gray().on("(coded with ❤  by igsp)"));
		System.out.println();
		System.out.println();

		VertecXml vertecXml = new VertecXml(noCache);

		final VertecCredentials vs = pickVertecCredentials(vertecXml);

		if (!vertecXml.authenticateUser(vs)) {
			System.err.println("Invalid credentials.. More luck next time!");
			return;
		}

		final User user = vertecXml.loadUser();
		System.out.println("Hello, " + Chalk256.chalk().green().on(user.getFullName()) + " :)");


		Integer projectId = null;
		Integer phaseId = null;
		Integer serviceTypeId = null;

		if (signature != null) {
			String[] split = StringUtil.splitc(signature, ',');
			projectId = Integer.parseInt(split[0].trim());
			phaseId = Integer.parseInt(split[1].trim());
			serviceTypeId = Integer.parseInt(split[2].trim());
		}

		// continue with picking

		final Project project;
		if (projectId == null) {
			project = pickAProject(vertecXml, user);
		}
		else {
			project = vertecXml.loadProject(projectId);
		}

		final Phase phase;
		if (phaseId == null) {
			phase = pickAPhase(vertecXml, project);
		}
		else {
			phase = vertecXml.loadPhase(phaseId);
		}

		final ServiceType serviceType;
		if (serviceTypeId == null) {
			serviceType = pickAServiceType(vertecXml, project);
		}
		else {
			serviceType = vertecXml.loadServiceType(serviceTypeId);
		}

		if (signature == null) {
			System.out.println();
			System.out.println(Chalk256.chalk().gray().on("TIP: you can pass this combination as: " + project.getId() + "," + phase.getId() + "," + serviceType.getId()));
		}


		final String description;
		if (message == null) {
			System.out.println();
			System.out.println("> Now enter a description:");
			description = readLine();
		}
		else {
			description = message;
		}


		final double time;
		if (hours == null) {
			System.out.println();
			System.out.println("> How much HOURS have you spent (you can enter decimals, too)?");
			final String timeString = readLine();
			time = Double.parseDouble(timeString);
		}
		else {
			time = Double.parseDouble(hours);
		}

		TimeEntry timeEntry = new TimeEntry();
		timeEntry
			.setProject(project)
			.setPhase(phase)
			.setServiceType(serviceType)
			.setDescription(description)
			.setMinutes((int) (time * 60));


		System.out.println();
		System.out.println(Chalk256.chalk().cyan().on("Lay back, the Vertec is being updated..."));

		vertecXml.storeTimeEntry(user, timeEntry, LocalDate.now());

		System.out.println(Chalk256.chalk().green().on("Done."));
		System.out.println();
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
		System.out.print("username: ");
		String username = readLine();
		System.out.print("password: ");
		String password = readLine();
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

		System.out.println();
		System.out.print("Select a project: ");
		String option = readLine();
		return projectList.get(Integer.valueOf(option.trim()) - 1);
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

		System.out.println();
		System.out.print("Select a phase: ");
		String option = readLine();
		return phasesList.get(Integer.valueOf(option.trim()) - 1);
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

		System.out.println();
		System.out.print("Select service type: ");
		String option = readLine();
		return serviceTypeList.get(Integer.valueOf(option.trim()) - 1);
	}

	// ---------------------------------------------------------------- utils

	private String readLine() {
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

}
