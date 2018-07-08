package com.morphism.search.coord.curator.joint;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 16:45
 */
public enum PathStatus {
	START(1),
	RUNNING(2),
	SUCCESS(3),
	FAILED(4),
	STOP(5);

	public final int		code;

	PathStatus(int code){
		this.code = code;
	}

}
