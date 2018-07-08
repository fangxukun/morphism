package com.morphism.search.coord.curator.joint;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 17:40
 */
public interface Function<T> {
	String call(T context) throws Exception;
	String stop();
	boolean isRunning();
}
