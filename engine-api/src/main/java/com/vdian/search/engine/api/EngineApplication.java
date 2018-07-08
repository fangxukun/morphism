package com.vdian.search.engine.api;

import com.vdian.search.engine.api.helloworld.HelloWorld;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 10:17
 */
@ApplicationPath("/")
public class EngineApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();

		classes.add(HelloWorld.class);
		return classes;
	}
}
