package com.vdian.engine.server.bootstrap;

import com.google.common.io.Resources;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.search.configuration.ResourceLoader;
import com.vdian.search.configuration.loader.MetaClientResourceLoader;
import com.vdian.search.netty.common.ServerLayout;
import com.vdian.search.netty.server.NettySolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * User: xukun.fyp
 * Date: 17/2/14
 * Time: 15:56
 */
public class MetaNettyServerMain {
	private static final Logger 		LOGGER			= LoggerFactory.getLogger(MetaNettyServerMain.class);
	private volatile CoreContainer		cores;
	private ResourceLoader 				loader;
	private EngineConfiguration 		configuration;
	private NettySolrServer 			nettyServer;

	public static void main(String[] args) throws IOException {
		MetaNettyServerMain bootstrap = new MetaNettyServerMain();
		bootstrap.init();
	}

	private void init() throws IOException {
		Properties config = loadFilterProperties();
		String metaZK = config.get("meta.zookeeper").toString();
		String metaNamespace = config.get("meta.namespace").toString();
		String engineConfig = config.get("engine.metaName").toString();

		loader = new MetaClientResourceLoader(metaZK,metaNamespace,engineConfig);
		loader.initSolrHome();
		configuration = loader.loadConfiguration();

		initCores();

		ServerLayout layout = configuration.getNodeConfiguration(ServerLayout.class, ServerLayout.NODE_NAME);
		nettyServer = new NettySolrServer(layout,cores);
		nettyServer.start();
		LOGGER.warn("netty server start complete!");
	}

	public void destroy(){
		nettyServer.shutdown();
	}

	private void initCores(){
		Path solrHome = configuration.currentSolrHome();
		NodeConfig solrXml = SolrXmlConfig.fromSolrHome(solrHome);
		cores = new CoreContainer(solrXml,null,true);
		cores.load();
	}

	private Properties loadFilterProperties() throws IOException {
		URL url = Resources.getResource(".");
		Path rootPath = Paths.get(url.getPath()).getParent().getParent().getParent();
		Path filterPath = Paths.get(rootPath.toString(), "filter.properties");

		Properties result = new Properties();
		result.load(Files.newInputStream(filterPath, StandardOpenOption.READ));

		return result;
	}
}
