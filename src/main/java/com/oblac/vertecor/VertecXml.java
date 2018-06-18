package com.oblac.vertecor;

import com.oblac.vertecor.model.Phase;
import com.oblac.vertecor.model.Project;
import com.oblac.vertecor.model.ServiceType;
import com.oblac.vertecor.model.TimeEntry;
import com.oblac.vertecor.model.User;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.Node;
import jodd.net.MimeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VertecXml {

	private final Cache cache;
	private VertecCredentials credentials;
	private String token;

	public VertecXml(final Cache cache) {
		this.cache = cache;
	}

	/**
	 * Authenticate user.
	 */
	public boolean authenticateUser(VertecCredentials vertecCredentials) {
		this.credentials = vertecCredentials;

		return Http.post("/auth/xml")
			.form("vertec_username", credentials.getUsername())
			.form("password", credentials.getPassword())
			.sendAndReceive(response -> {
				this.token = response.bodyText();
				return response.statusCode() == 200;
			});
	}

	public User loadUser() {
		HttpResponse httpResponse = post(
				"<Query>" +
				"<Selection><ocl>Projektbearbeiter</ocl>" +
				"<sqlwhere>aktiv = 1</sqlwhere>" +
				"<sqlwhere>kuerzel = '" + credentials.getUsername() + "'</sqlwhere>" +
				"</Selection>" +
				"<Resultdef>" +
				"    <member>name</member>" +
				"    <member>kuerzel</member>" +
				"    <member>bearbProjekte</member>" +
				" </Resultdef>" +
				"</Query>")
			.send();

		String body = httpResponse.bodyText();
		Document doc = new LagartoDOMBuilder().enableXmlMode().parse(body);
		Node projektBearbeiterNode = doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild();

		return new User(
			nodeChildText(projektBearbeiterNode, "objid"),
			credentials.getUsername(),
			nodeChildText(projektBearbeiterNode, "name"),
			nodeChildIdList(projektBearbeiterNode, "bearbProjekte"));
	}

	/**
	 * Loads a project with given ID.
	 */
	public Project loadProject(int projectId) {
		return cache.fromCache(projectId, Project.class, () ->
			post("<Query>" +
				"<Selection>" +
				"<objref>" + projectId + "</objref>" +
				"</Selection>" +
				"<Resultdef>" +
				"    <member>code</member>" +
				"    <member>beschrieb</member>" +
				"    <member>taetigkeiten</member>" +
				"    <member>aktiv</member>" +
				"    <member>phasen</member>" +
				" </Resultdef>" +
				"</Query>")
			.sendAndReceive(response -> {
				String body = response.bodyText();
				Document doc = new LagartoDOMBuilder().enableXmlMode().parse(body);
				Node projektNode = doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild();

				return new Project()
					.setId(nodeChildInt(projektNode, "objid"))
					.setCode(nodeChildText(projektNode, "code"))
					.setDescription(nodeChildText(projektNode, "beschrieb"))
					.setActive(nodeChildBoolean(projektNode, "aktiv"))
					.setServiceTypes(nodeChildIdList(projektNode, "taetigkeiten"))
					.setPhases(nodeChildIdList(projektNode, "phasen"));
			}));
	}

	public Phase loadPhase(int phaseId) {
		return cache.fromCache(phaseId, Phase.class, () ->
			post("<Query>" +
				"<Selection>" +
				"<objref>" + phaseId + "</objref>" +
				"</Selection>" +
				"<Resultdef>" +
				"    <member>code</member>" +
				"    <member>planwertext</member>" +
				"    <member>aktiv</member>" +
				"    <member>sublist</member>" +
				" </Resultdef>" +
				"</Query>")
				.sendAndReceive(response -> {
					String body = response.bodyText();
					Document doc = new LagartoDOMBuilder().enableXmlMode().parse(body);
					Node phaseNode = doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild();

					return new Phase()
						.setId(nodeChildInt(phaseNode, "objid"))
						.setActive(nodeChildBoolean(phaseNode, "aktiv"))
						.setSubphases(nodeChildIdList(phaseNode, "sublist"))
						.setCode(nodeChildText(phaseNode, "code"));
				}));
	}

	public ServiceType loadServiceType(int serviceTypeId) {
		return cache.fromCache(serviceTypeId, ServiceType.class, () ->
			post("<Query>" +
				"<Selection>" +
				"<objref>" + serviceTypeId + "</objref>" +
				"</Selection>" +
				"<Resultdef>" +
				"    <member>aktiv</member>" +
				"    <member>code</member>" +
				" </Resultdef>" +
				"</Query>")
				.sendAndReceive(response -> {
					String body = response.bodyText();
					Document doc = new LagartoDOMBuilder().enableXmlMode().parse(body);
					Node phaseNode = doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild();

					return new ServiceType()
						.setId(nodeChildInt(phaseNode, "objid"))
						.setActive(nodeChildBoolean(phaseNode, "aktiv"))
						.setCode(nodeChildText(phaseNode, "code"));
				}));
	}

	// ---------------------------------------------------------------- all

	/**
	 * Loads all user projects.
	 */
	public List<Project> loadUserActiveProjects(User user) {
		return user.getProjectIds().stream()
			.map(this::loadProject)
			.filter(Project::isActive)
			.collect(Collectors.toList());
	}

	/**
	 * Loads all phases. Some phases has the subphases that has to be loaded as well.
	 */
	public List<Phase> loadProjectPhases(Project project) {
		List<Phase> allPhases = new ArrayList<>();

		project.getPhases().stream()
			.map(this::loadPhase)
			.filter(Phase::isActive)
			.forEach(phase -> {
				if (phase.getSubphases().isEmpty()) {
					allPhases.add(phase);
				} else {
					allPhases.addAll(loadSubphases(phase));
				}
			});
		return allPhases;
	}

	private List<Phase> loadSubphases(Phase phase) {
		List<Phase> allSubphases = new ArrayList<>();

		phase.getSubphases().stream()
			.map(this::loadPhase)
			.filter(Phase::isActive)
			.forEach(p -> {
				if (p.getSubphases().isEmpty()) {
					allSubphases.add(p);
				} else {
					allSubphases.addAll(loadSubphases(p));
				}
			});

		return allSubphases;
	}

	public List<ServiceType> loadServiceTypes(Project project) {
		return project.getServiceTypes().stream()
			.map(this::loadServiceType)
			.collect(Collectors.toList());
	}



	// ---------------------------------------------------------------- create

	public boolean storeTimeEntry(User user, TimeEntry timeEntry) {
        return post(
        	"<Create><OffeneLeistung>" +
		        "<bearbeiter><objref>" + user.getUserId() + "</objref></bearbeiter>" +
		        "<projekt><objref>" + timeEntry.getProject().getId() + "</objref></projekt>" +
		        "<phase><objref>" + timeEntry.getPhase().getId() + "</objref></phase>" +
		        "<typ><objref>" + timeEntry.getServiceType().getId() + "</objref></typ>" +
		        "<minutenInt>" + timeEntry.getMinutes() + "</minutenInt>" +
		        "<text>" + timeEntry.getDescription() + "</text>" +
		        "<datum>" + timeEntry.getDate() + "</datum>"+
	        "</OffeneLeistung></Create>"
        ).sendAndReceive(httpResponse -> httpResponse.statusCode() == 200);
	}

	// ---------------------------------------------------------------- vs

	/**
	 * Reads credential from the cache or {@code null} if not found.
	 */
	public VertecCredentials credentialsFromCache(VertecCredentials vc) {
		return cache.fromCache(0, VertecCredentials.class, () -> vc);
	}


	// ---------------------------------------------------------------- utils

	private HttpRequest post(String body) {
		return Http.post("/xml")
			.contentType(MimeTypes.MIME_APPLICATION_XML)
			.body("<Envelope><Header><BasicAuth><Token>" + token +
				"</Token></BasicAuth></Header><Body>" + body +
				"</Body></Envelope>");
	}

	/**
	 * Extracts the text content of a chile node with given name.
	 */
	private String nodeChildText(Node node, String childNodeName) {
		return node.findChildNodeWithName(childNodeName).getTextContent().trim();
	}

	private int nodeChildInt(Node node, String childNodeName) {
		return Integer.valueOf(node.findChildNodeWithName(childNodeName).getTextContent().trim());
	}

	private boolean nodeChildBoolean(Node node, String nodeName) {
		return node.findChildNodeWithName(nodeName).getTextContent().trim().equals("1");
	}

	/**
	 * Collects all the ids listed in specified child node.
	 */
	private List<Integer> nodeChildIdList(Node node, String childNodeName) {
		List<Integer> list = new ArrayList<>();
		Node[] ids = node.findChildNodeWithName(childNodeName).getFirstChild().getChildNodes();
		for (Node id : ids) {
			list.add(Integer.valueOf(id.getTextContent().trim()));
		}
		return list;
	}

}
