package com.morphism.engine.server;

import com.google.common.base.Preconditions;
import com.morphism.search.commons.EngineStatus;
import com.morphism.search.commons.HttpPayload;
import com.morphism.search.commons.RestHttpClient;
import com.morphism.search.commons.UrlPaths;

import java.io.IOException;
import java.net.URI;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 14:05
 */
public class AdminClient {
	private final RestHttpClient 		httpClient;
	private final String 				adminBaseUrl;

	public AdminClient(String adminBaseUrl){
		Preconditions.checkArgument(UrlPaths.checkBaseUrl(adminBaseUrl),"admin base url is invalid,format like: http://host:port/");
		this.httpClient = new RestHttpClient();
		this.adminBaseUrl = adminBaseUrl;
	}

	public String reportEngineStatus(EngineStatus status){
		URI uri = URI.create(UrlPaths.getUrl(adminBaseUrl,"/api/engine/status"));
		return httpClient.httpPostSilent(uri, HttpPayload.create().setPayload(status).toJson());
	}

	public void lookupSelf() throws IOException {
		URI uri = URI.create(UrlPaths.getUrl(adminBaseUrl, "/api/engine/self"));
		String response = httpClient.httpGet(uri);

	}
}
