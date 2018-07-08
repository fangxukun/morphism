package com.vdian.search.coord.curator.joint;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 17:58
 */
public enum CommonSteps implements Step{
	STEP_1(10),
	STEP_2(20),
	STEP_3(30),
	STEP_4(40),
	STEP_5(50);

	public final int 	code;

	private CommonSteps(int code){
		this.code = code;
	}


	@Override
	public int code() {
		return code;
	}
}
