package com.morphism.engine.server.bootstrap;

import com.google.common.io.Resources;
import com.morphism.search.configuration.EngineConfiguration;
import com.morphism.search.configuration.ResourceLoader;
import com.morphism.search.configuration.loader.LocalFileResourceLoader;
import com.morphism.search.netty.common.ServerLayout;
import com.morphism.search.netty.server.NettySolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/4/27
 * Time: 14:54
 */
public class FileNettyServerMain {
	private static final Logger 		LOGGER			= LoggerFactory.getLogger(FileNettyServerMain.class);
	private final String				resourceName	= "vitem";

	private volatile CoreContainer 		cores;
	private ResourceLoader 				loader;
	private EngineConfiguration 		configuration;
	private NettySolrServer 			nettyServer;

	public static void main(String[] args) throws IOException {
//		FileNettyServerMain bootstrap = new FileNettyServerMain();
//		bootstrap.init();
		System.out.println(19186787%1024);
		System.out.println((19186787%1024)/64);
	}

	public void init() throws IOException {
		Path localPath = Paths.get(Resources.getResource(resourceName).getPath());
		loader = new LocalFileResourceLoader(localPath);
		loader.initSolrHome();

		configuration = loader.loadConfiguration();

		initCores();

		ServerLayout layout = configuration.getNodeConfiguration(ServerLayout.class, ServerLayout.NODE_NAME);
		nettyServer = new NettySolrServer(layout,cores);
		nettyServer.start();
		LOGGER.warn("netty server start complete!");
	}

	public void sync() throws InterruptedException {
		nettyServer.sync();
	}

	public int getPort(){
		return this.configuration.nettyPort;
	}

	public EngineConfiguration getConfiguration(){
		return configuration;
	}

	public void destroy(){
		nettyServer.shutdown();
	}

	private void initCores(){
		Path solrHome = configuration.currentSolrHome();
		NodeConfig solrXml = SolrXmlConfig.fromSolrHome(solrHome);
		cores = new CoreContainer(solrXml,null);
		cores.load();
	}


	public CoreContainer getCores(){
		return cores;
	}
}
