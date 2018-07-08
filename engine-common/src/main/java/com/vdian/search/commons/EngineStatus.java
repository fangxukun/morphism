package com.vdian.search.commons;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 11:54
 */
public enum EngineStatus {
	START_FETCH_RESOURCE(1),
	START_INIT_SOLR_HOME(2),
	START_INIT_SOLR(3),
	START_INIT_NETTY(4);



	public final int  		code;

	EngineStatus(int code){
		this.code = code;
	}
}
