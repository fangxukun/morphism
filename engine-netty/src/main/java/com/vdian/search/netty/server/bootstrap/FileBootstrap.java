package com.vdian.search.netty.server.bootstrap;

import com.google.common.io.Resources;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.search.configuration.ResourceLoader;
import com.vdian.search.configuration.loader.LocalFileResourceLoader;
import com.vdian.search.netty.common.ServerLayout;
import com.vdian.search.netty.server.NettySolrServer;
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
 * Date: 17/3/27
 * Time: 16:08
 */
public class FileBootstrap {
	private static final Logger			LOGGER			= LoggerFactory.getLogger(FileBootstrap.class);
	private volatile CoreContainer 		cores;
	private ResourceLoader 				loader;
	private EngineConfiguration 		configuration;
	private NettySolrServer 			nettyServer;

	public static void main(String[] args) throws IOException {
		FileBootstrap bootstrap = new FileBootstrap();
		bootstrap.init();
	}

	public void initConfiguration(){
		this.initConfiguration("vitem");
	}
	public void initConfiguration(String resourceName){
		Path localPath = Paths.get(Resources.getResource(resourceName).getPath());
		loader = new LocalFileResourceLoader(localPath);
		configuration = loader.loadConfiguration();
	}

	public void init() throws IOException {
		if(this.configuration == null){
			initConfiguration();
		}
		loader.initSolrHome();

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
