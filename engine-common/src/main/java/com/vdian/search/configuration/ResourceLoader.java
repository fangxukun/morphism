package com.vdian.search.configuration;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * User: xukun.fyp
 * Date: 16/12/1
 * Time: 15:25
 * 抽离出来，目前有MetaClient的实现，如果需要在本地加载/DB中加载/...自由实现.
 */
public abstract class ResourceLoader {
	private static final Logger 		LOGGER			= LoggerFactory.getLogger(ResourceLoader.class);
	protected EngineConfiguration		configuration;

	public ResourceLoader(){

	}

	protected void init(EngineConfiguration configuration){
		this.configuration = configuration;
	}

	public EngineConfiguration loadConfiguration() {
		return configuration;
	}

	public void initSolrHome() {
		try{
			Path solrHome = Paths.get(this.configuration.solrHome);
			Path newTempPath = SolrHomePaths.currentTempPath(solrHome);

			FileUtils.deleteDirectory(newTempPath.toFile());
			Files.createDirectories(newTempPath);

			//1.solr.xml
			Path solrXmlPath = SolrHomePaths.solrXml(newTempPath);
			byte[] solrXmlContent = readContent(this.configuration.solrXml);
			Files.write(solrXmlPath,solrXmlContent, StandardOpenOption.CREATE);

			for(EngineConfiguration.CoreConfiguration core : this.configuration.coreConfigurations){
				//2. core directory
				Path corePath = SolrHomePaths.corePath(newTempPath,core.coreName);
				Files.createDirectories(corePath);

				//3. core conf directory
				Path coreConfPath = SolrHomePaths.coreConfPath(newTempPath, core.coreName);
				Files.createDirectories(coreConfPath);

				//4. core.properties file
				Path corePropPath = SolrHomePaths.corePropertiesPath(newTempPath,core.coreName);
				Files.write(corePropPath,generateCorePropContent(core.coreName),StandardOpenOption.CREATE);

				//5. schema.xml
				Path schemaPath = SolrHomePaths.schemaPath(newTempPath, core.coreName);
				byte[] schemaXmlContent = readContent(core.schemaXml);
				Files.write(schemaPath,schemaXmlContent,StandardOpenOption.CREATE);

				//6. solrConfig.xml
				Path solrConfigPath = SolrHomePaths.solrConfigPath(newTempPath,core.coreName);
				byte[] solrConfigXmlContent = readContent(core.solrConfigXml);
				Files.write(solrConfigPath,solrConfigXmlContent,StandardOpenOption.CREATE);
			}


			SolrHomePaths.switchPath(solrHome,newTempPath);
			LOGGER.warn("[ENGINE-BOOTSTRAP] solrHome init complete!");
		}catch (Exception e){
			throw new RuntimeException("[ENGINE-BOOTSTRAP] touch solr home failed",e);
		}
	}

	/**
	 * read content of the config key in configuration, include solr.xml,schema.xml,solrConfig.xml
	 * @param configKey
	 * @return
	 * @throws IOException
	 */
	public abstract byte[] readContent(String configKey) throws IOException;

	private byte[] generateCorePropContent(String coreName){
		StringBuilder builder = new StringBuilder();
		builder.append("dataDir=").append(configuration.dataHome).append("/").append(coreName).append("/");
		return builder.toString().replaceAll("//","/").getBytes(Charsets.UTF_8);
	}

	public abstract String getLoaderName();
}
