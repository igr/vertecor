package com.oblac.vertecor;

import jodd.http.Cookie;
import jodd.json.JsonParser;

import java.util.Map;

public class VertecWeb {

	private final VertecCredentials credentials;
	private Cookie[] cookies;
	private String sessionId;
	private String connectionToken;

	public VertecWeb(VertecCredentials vertecCredentials) {
		this.credentials = vertecCredentials;
	}

	public boolean loginUser() {
		System.out.println("Login...");

		return Http.post("/webapp/")
			.form("vertec_username", credentials.getUsername())
			.form("password", credentials.getPassword())
			.sendAndReceive(response -> {
				this.cookies = response.cookies();
				return response.statusCode() == 200;
			});
	}

	public String resolveSessionIdFromCookies() {
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("vertec_session_id")) {
				this.sessionId = cookie.getValue();
				break;
			}
		}
		return sessionId;
	}

	public String negotiateConnectionToken() {
		System.out.println("Negotiating...");

		return Http.get("/uisync/negotiate")
			.query("clientProtocol", "1.5")
			.cookies(cookies)
			.sendAndReceive(response -> {
					this.connectionToken = JsonParser
						.create()
						.<Map<String, String>>parse(response.bodyText())
						.get("ConnectionToken");
					return connectionToken;
				}
			);
	}


	//      WebSocketImpl.DEBUG = true;
//		Socketeer s = new Socketeer(new URI(
//			"wss://vertec.zuehlke.com/uisync/connect?
//              transport=webSockets&clientProtocol=1.5&connectionToken=" + URLCoder.encodeQueryParam(connectionToken)), headers);
//		s.connect();


}
