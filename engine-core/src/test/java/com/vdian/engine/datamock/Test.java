package com.vdian.engine.datamock;

import org.apache.lucene.util.MathUtil;

/**
 * User: xukun.fyp
 * Date: 16/12/5
 * Time: 12:18
 */
public class Test {
	@org.junit.Test
	public void test1(){
		System.out.println(1L << 65);
	}

	@org.junit.Test
	public void test2(){
		System.out.println(MathUtil.gcd(90,34));
	}
}
