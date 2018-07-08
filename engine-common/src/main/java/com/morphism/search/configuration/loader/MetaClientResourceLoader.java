package com.morphism.search.configuration.loader;

import com.google.common.base.Charsets;
import com.koudai.rio.commons.properties.JsonProperties;
import com.koudai.rio.commons.properties.JsonPropertyUtils;
import com.vdian.ergate.meta.client.MetaClient;
import com.vdian.ergate.meta.common.curator.CuratorAccessApi;
import com.vdian.ergate.meta.common.io.MetaConfig;
import com.vdian.ergate.meta.common.io.MetaName;
import com.morphism.search.configuration.EngineConfiguration;
import com.morphism.search.configuration.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 16/12/1
 * Time: 15:27
 * 通过MetaClient初始化引擎的配置资源。
 */
public class MetaClientResourceLoader extends ResourceLoader {
	public static final String 			LOADER_NAME		=	"meta";
	private static final Logger			LOGGER			= LoggerFactory.getLogger(MetaClientResourceLoader.class);

	private final MetaClient			client;

	public MetaClientResourceLoader(String metaZooKeeper, String metaNamespace, String engineMetaName){
		try{
			client = new MetaClient(metaZooKeeper,metaNamespace);

			String engineConf = loadConfig(engineMetaName);
			JsonProperties root = JsonPropertyUtils.loadProperty(engineConf);

			init(new EngineConfiguration(root));
			LOGGER.info("load engine configuration complete!",configuration);
		}catch (Exception e){
			throw new RuntimeException("[ENGINE-BOOTSTRAP] init meta client failed",e);
		}
	}

	public MetaClientResourceLoader(CuratorAccessApi api,String engineMetaName) {
		try{
			this.client = new MetaClient(api);
			String engineConf = loadConfig(engineMetaName);
			JsonProperties root = JsonPropertyUtils.loadProperty(engineConf);

			init(new EngineConfiguration(root));
			LOGGER.info("load engine configuration complete!",configuration);
		}catch (Exception e){
			throw new RuntimeException("[ENGINE-BOOTSTRAP] init meta client failed",e);
		}
	}

	@Override
	public byte[] readContent(String configKey) throws IOException {
		MetaConfig mc = client.fetcher().loadByName(MetaName.create(configKey));
		return mc.getContent().getBytes(Charsets.UTF_8);
	}

	private String loadConfig(String name) throws IOException {
		MetaConfig mc = client.fetcher().loadByName(MetaName.create(name));
		return mc.getContent();
	}


	public static void main(String[] args) {
		MetaClientResourceLoader filter = new MetaClientResourceLoader("10.1.100.75:2181,10.1.100.76:2181,10.1.100.77:2181","hzsearch-meta","engines.vitem");
		filter.initSolrHome();
	}


	@Override
	public String getLoaderName() {
		return LOADER_NAME;
	}
}
