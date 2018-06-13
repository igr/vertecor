package com.oblac.vertecor;

import jodd.http.HttpRequest;

public class Http {

	public static HttpRequest get(String path) {
		return HttpRequest.get("https://vertec.zuehlke.com" + path)
			.header("Origin", "https://vertec.zuehlke.com");
	}
	public static HttpRequest post(String path) {
		return HttpRequest.post("https://vertec.zuehlke.com" + path)
			.header("Origin", "https://vertec.zuehlke.com");
	}

}
