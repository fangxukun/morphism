package com.morphism.search.configuration.loader;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.koudai.rio.commons.properties.JsonProperties;
import com.koudai.rio.commons.properties.JsonPropertyUtils;
import com.morphism.search.configuration.EngineConfiguration;
import com.morphism.search.configuration.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/3/27
 * Time: 17:04
 */
public class LocalFileResourceLoader extends ResourceLoader {
	public static final String 			LOADER_NAME					=	"file";
	private static final String 		FILE_NAME_CONFIGURATION		=	"configuration.json";
	private final Path					localDir;


	public LocalFileResourceLoader(Path localDir){
		try{
			this.localDir = localDir;
			String configContent = readAll(localDir.resolve(FILE_NAME_CONFIGURATION));
			JsonProperties root = JsonPropertyUtils.loadProperty(configContent);

			init(new EngineConfiguration(root));
		}catch (Exception e){
			throw new RuntimeException("[ENGINE-BOOTSTRAP] init from local file failed,localDir:" + localDir,e);
		}
	}

	/**
	 * resolve the config key as local file name
	 * @param configKey
	 * @return
	 * @throws IOException
	 */
	@Override
	public byte[] readContent(String configKey) throws IOException {
		Path path = localDir.resolve(configKey);
		if(!Files.exists(path)){
			path = localDir.resolve(configKey + ".xml");
		}
		return Files.readAllBytes(path);
	}

	private String readAll(Path path) throws IOException {
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes, Charsets.UTF_8);
	}


	public static void main(String[] args){
		String pathStr = Resources.getResource("engines/vitem").getPath();
		Path localPath = Paths.get(pathStr);
		LocalFileResourceLoader rl = new LocalFileResourceLoader(localPath);
		rl.initSolrHome();
	}

	@Override
	public String getLoaderName() {
		return LOADER_NAME;
	}
}
