package com.morphism.engine.server.engine.full;

import com.morphism.engine.server.engine.data.IndexFetcher;
import com.morphism.engine.server.engine.data.Indexes;
import com.morphism.search.configuration.EngineConfiguration;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/26
 * Time: 09:45
 * 全量索引切换器
 */
public class IndexSwitcher {
	private final HadoopConfLayout		layout;
	private final CoreContainer			container;

	public IndexSwitcher(EngineConfiguration configuration,CoreContainer container){
		this.layout = configuration.getNodeConfiguration(HadoopConfLayout.class,"hadoop-layout");
		this.container = container;
	}


	public void switchIndex(String coreName,RemoteLocations.RemoteLocation location,int limitMB) throws IOException, InterruptedException {
		SolrCore core = container.getCore(coreName);
		IndexFetcher fetcher = new IndexFetcher(core);

		String newIndexDir = null;
		switch (location.remoteType){
			case RemoteLocations.HDFS_TYPE:
				newIndexDir =fetcher.fetchFromHDFS(layout.configuration,location.remotePath);
				break;
			case RemoteLocations.SERVER_TYPE:
				newIndexDir = fetcher.fetchFromServer(location.serverIp,location.remotePath,limitMB);
				break;
			default:
				throw new RuntimeException("unsupported location type " + location.remoteType);
		}

		Indexes.switchIndex(core,newIndexDir);
	}


}
