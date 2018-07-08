package com.vdian.engine.server.engine.recovery;

/**
 * User: xukun.fyp
 * Date: 17/5/17
 * Time: 15:27
 */
public class TargetServer {
	public final String 		ip;
	public final String 		baseUrlWithCore;


	public TargetServer(String ip, String baseUrlWithCore) {
		this.ip = ip;
		this.baseUrlWithCore = baseUrlWithCore;
	}
}
