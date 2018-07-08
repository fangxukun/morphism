package com.vdian.engine.server.engine;

import com.google.common.io.Resources;
import com.vdian.engine.server.EngineContext;
import com.vdian.engine.server.cloud.VModelCloud;
import com.vdian.engine.server.engine.recovery.PeerRecovery;
import com.vdian.engine.server.servlet.StatusCheck;
import com.vdian.ergate.meta.common.curator.CuratorAccessApi;
import com.vdian.search.commons.EngineStatus;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.search.configuration.ResourceLoader;
import com.vdian.search.configuration.loader.LocalFileResourceLoader;
import com.vdian.search.configuration.loader.MetaClientResourceLoader;
import com.vdian.search.netty.common.ServerLayout;
import com.vdian.search.netty.server.NettySolrServer;
import com.vdian.search.sync.PathSyncServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 15:27
 */
public class SolrEngine {
	private static final Logger 	LOGGER		= LoggerFactory.getLogger(SolrEngine.class);
	private final FilterConfig		config;

	private CuratorAccessApi 		curatorApi;
	private ResourceLoader			resourceLoader;
	private EngineConfiguration		configuration;
	private EngineContext			engineContext;
	private NettySolrServer			nettyServer;
	private VModelCloud				vModelCloud;
	private PathSyncServer			pathServer;
	private PeerRecovery			recovery;

	public SolrEngine(FilterConfig config){
		this.config = config;
	}

	public void init() throws Exception {
		String resourceType = config.getInitParameter("resourceType");
//		System.out.println("resourceType:" + resourceType);
		switch (resourceType){
			case LocalFileResourceLoader.LOADER_NAME:
				String localPathDirName = config.getInitParameter("localPath");
				Path localPath = Paths.get(Resources.getResource(localPathDirName).getPath());
				resourceLoader = new LocalFileResourceLoader(localPath);
				break;

			case MetaClientResourceLoader.LOADER_NAME:
				String metaZookeeper = config.getInitParameter("metaZookeeper");
				String metaNamespace = config.getInitParameter("metaNamespace");
				String engineMetaName = config.getInitParameter("engineMetaName");
				this.curatorApi = new CuratorAccessApi(metaZookeeper,metaNamespace);
				resourceLoader = new MetaClientResourceLoader(curatorApi,engineMetaName);
				break;
		}

		engineContext = new EngineContext(curatorApi);
		engineContext.reportEngineStatus(EngineStatus.START_FETCH_RESOURCE);
		configuration = this.resourceLoader.loadConfiguration();

		vModelCloud = new VModelCloud(configuration);
		vModelCloud.init();

		pathServer = new PathSyncServer();
		pathServer.start();
		this.initSolrHome();

		LOGGER.warn("[STARTUP]Solr Engine init complete!");
	}


	public void initCores(CoreContainer container) throws Exception {
		startNettyServer(container);
		LOGGER.warn("[STARTUP] start netty complete!");

		for(SolrCore core : container.getCores()){
			try{
				recovery = new PeerRecovery(vModelCloud,core);
				recovery.recoveryOnStartup();
				LOGGER.warn("[STARTUP] recovery core:{} complete!",core.getName());
			}catch (Exception e){
				LOGGER.warn("[STARTUP] recovery core:" + core.getName() + " failed!",e);
			}
		}

		vModelCloud.onlineWriter();
		vModelCloud.onlineReader();
	}

	private void initSolrHome(){
		this.resourceLoader.initSolrHome();
		config.getServletContext().setAttribute(SolrDispatchFilter.SOLRHOME_ATTRIBUTE, configuration.currentSolrHome().toString());
		engineContext.reportEngineStatus(EngineStatus.START_FETCH_RESOURCE);
	}

	public void startNettyServer(CoreContainer container){
		ServerLayout layout = configuration.getNodeConfiguration(ServerLayout.class,ServerLayout.NODE_NAME);
		nettyServer = new NettySolrServer(layout,container);
		nettyServer.start();
		engineContext.reportEngineStatus(EngineStatus.START_INIT_NETTY);
		StatusCheck.RUNNING.set(true);
	}

	// just for test
	@Deprecated
	public void nettySync() throws InterruptedException {
		nettyServer.sync();
	}

	public void destroy(){
		try{
			vModelCloud.offlineWriter();
			vModelCloud.offlineReader();
		}catch (Exception e){
			LOGGER.error("offline failed!");
		}

		if(curatorApi != null){
			curatorApi.close();
		}

		if(nettyServer != null){
			nettyServer.shutdown();
		}

	}
}
