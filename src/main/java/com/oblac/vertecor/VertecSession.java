package com.oblac.vertecor;

import com.oblac.vertecor.model.Phase;
import com.oblac.vertecor.model.Project;
import com.oblac.vertecor.model.ServiceType;
import com.oblac.vertecor.model.TimeEntry;
import com.oblac.vertecor.model.User;
import jodd.chalk.Chalk256;
import jodd.util.function.Maybe;

import java.util.List;
import java.util.Objects;

/**
 * Represents current vertec session, the state.
 */
public class VertecSession {

	private final VertecXml vertecXml;
	private User user;

	public VertecSession(boolean noCache, boolean clearCache) {
		final Cache cache = new Cache(noCache, clearCache);
		this.vertecXml = new VertecXml(cache);
	}

	public User getUser() {
		return user;
	}
	/**
	 * Loads stored Vertec credentials.
	 */
	public Maybe<VertecCredentials> loadCachedVertecCredentials() {
		return Maybe.of(vertecXml.credentialsFromCache(null));
	}

	/**
	 * Authenticates and loads user from the Vertec.
	 */
	public User authAndLoadUser(VertecCredentials vc) {
		if (!vertecXml.authenticateUser(vc)) {
			System.err.println("Invalid credentials. More luck next time!");
			System.exit(1);
			return null;
		}

		vertecXml.credentialsFromCache(vc);

		this.user = vertecXml.loadUser();

		System.out.println("Hello, " + Chalk256.chalk().green().on(user.getFullName()) + " :)");

		return user;
	}

	/**
	 * Stores populated time entry.
	 */
	public boolean storeTimeEntry(TimeEntry timeEntry) {
		timeEntry.setUser(user);

		System.out.println();
		System.out.println(Chalk256.chalk().cyan().on("Lay back, the Vertec is being updated..."));

		boolean success = vertecXml.storeTimeEntry(timeEntry);

		if (success) {
			System.out.println(Chalk256.chalk().green().on("Done."));
			System.out.println();
		}
		else {
			System.out.println("Something went wrong!");
			System.out.println();
		}

		return success;
	}

	public Project loadProject(int projectId) {
		return Objects.requireNonNull(vertecXml.loadProject(projectId));
	}

	public List<Project> loadAllProjects() {
		return vertecXml.loadUserActiveProjects(user);
	}

	public Phase loadProjectPhase(int phaseId) {
		return Objects.requireNonNull(vertecXml.loadPhase(phaseId));
	}

	public List<Phase> loadAllProjectPhases(Project project) {
		return vertecXml.loadProjectPhases(project);
	}

	public ServiceType loadServiceType(int serviceTypeId) {
		return Objects.requireNonNull(vertecXml.loadServiceType(serviceTypeId));
	}

	public List<ServiceType> loadAllServiceTypes(Project project) {
		return vertecXml.loadServiceTypes(project);
	}

}
