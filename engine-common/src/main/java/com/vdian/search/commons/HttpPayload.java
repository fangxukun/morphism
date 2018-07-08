package com.vdian.search.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 14:30
 */
public class HttpPayload<Payload> {
	private String 			requestIp;
	private Payload			payload;

	public HttpPayload(){
		this.requestIp = NetworkUtils.localIp();
	}

	public static HttpPayload create(){
		return new HttpPayload();
	}

	public String getRequestIp() {
		return requestIp;
	}

	public Payload getPayload() {
		return payload;
	}

	public HttpPayload setPayload(Payload payload) {
		this.payload = payload;
		return this;
	}

	public String toJson(){
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

	public static <Payload> HttpPayload<Payload> fromJson(String json,Class<Payload> payloadClass){
		HttpPayload<Payload> result = new HttpPayload<>();

		Gson gson = new GsonBuilder().create();
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(json).getAsJsonObject();

		result.requestIp = root.get("requestIp").getAsString();
		result.payload = gson.fromJson(root.get("payload"),payloadClass);
		return result;
	}
}
