package com.morphism.search.netty.client;

/**
 * User: xukun.fyp
 * Date: 16/12/13
 * Time: 18:27
 */
public enum ChannelHandler {
	SOLR("solr"),
	DECODER("decoder"),
	ENCODER("encoder"),
	PROTO_DECODER("proto_decoder"),
	PROTO_ENCODER("proto_encoder"),
	DISCARD("discard"),
	HEARTBEAT("heartbeat");


	public final String name;
	private ChannelHandler(String name){
		this.name = name;
	}
}
