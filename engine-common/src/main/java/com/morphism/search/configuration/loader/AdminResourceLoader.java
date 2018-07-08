package com.morphism.search.configuration.loader;

import com.morphism.search.configuration.ResourceLoader;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 15:08
 */
public class AdminResourceLoader extends ResourceLoader{
	public static final String 			LOADER_NAME					=	"admin";



	@Override
	public byte[] readContent(String configKey) throws IOException {
		return new byte[0];
	}

	@Override
	public String getLoaderName() {
		return null;
	}
}
