package com.vdian.search.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.koudai.rio.commons.properties.JsonProperties;
import com.koudai.rio.commons.properties.JsonPropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 16/11/30
 * Time: 11:09
 * 本配置将兼容之前的vModel模式。对于SolrCloud模式，可以启用新的配置完成。
 */
public class EngineConfiguration {
	private static final Logger				LOGGER					= 	LoggerFactory.getLogger(EngineConfiguration.class);
	public static final String 				DEFAULT_SOLR_HOME		=	"/home/www/solr-home";
	public static final String 				DEFAULT_DATA_HOME		=	"/home/www/data-home";
	public static final int					DEFAULT_NETTY_PORT		=	8008;
	public static final int 				DEFAULT_HTTP_PORT		=	8080;

	//vModel使用的Zookeeper地址
	public final String						zookeeper;

	//引擎的服务名，VModel对应的serviceName
	public final String 					serviceName;

	//SolrCore 配置目录
	public final String						solrHome;
	//SolrCore 数据目录
	public final	String					dataHome;
	//Netty服务对外端口
	public final int	 					nettyPort;
	//Http服务对外端口
	public final int 						httpPort;

	//solr.xml的配置Id,
	public final String						solrXml;
	//引擎对应的行列分布
	public final EngineMatrix				engineMatrix;
	public final CoreConfiguration[]		coreConfigurations;
	public final JsonProperties				root;



	public EngineConfiguration(JsonProperties root){
		LOGGER.warn("EngineConfiguration:" + root.toPrettyString());

		try{
			this.root = root;
			this.zookeeper = root.getStringNotNull("zookeeper");
			this.serviceName = root.getStringNotNull("service-name");

			this.solrHome = root.getString("solr-home", DEFAULT_SOLR_HOME);
			this.dataHome = root.getString("data-home", DEFAULT_DATA_HOME);
			this.nettyPort = root.getInteger("netty-port", DEFAULT_NETTY_PORT);
			this.httpPort = root.getInteger("http-port", DEFAULT_HTTP_PORT);

			this.solrXml = root.getString("solr-xml");
			this.engineMatrix = new EngineMatrix(root.getArrayNotNull("engine-matrix"));

			List<JsonProperties> cores = root.getArray("cores");
			this.coreConfigurations = new CoreConfiguration[cores.size()];
			for(int i=0;i<cores.size();i++){
				this.coreConfigurations[i] = new CoreConfiguration(cores.get(i));
			}

		}catch (Exception e){
			LOGGER.error("EngineConfiguration parse failed",e);
			throw new RuntimeException(e);
		}

		LOGGER.warn("EngineConfiguration init success!", this.toString());
	}

	public Path currentSolrHome(){
		return Paths.get(solrHome,SolrHomePaths.CURRENT);
	}

	public <T> T getNodeConfiguration(Class<T> clazz,String nodeName){
		JsonProperties node = this.root.getSubProperties(nodeName);
		if(node != null){
			try{
				Constructor<T> constructor = clazz.getConstructor(new Class[]{JsonProperties.class});
				return constructor.newInstance(node);
			}catch (Exception e){
				throw new RuntimeException(String.format("get node configuration failed,clazz:%s,nodeNode:%s",clazz.getName(),nodeName));
			}
		}
		return null;
	}


	public static class CoreConfiguration{
		public final String 				coreName;
		public final String 				schemaXml;
		public final String 				solrConfigXml;

		public CoreConfiguration(JsonProperties root){
			this.coreName = root.getString("core-name");
			this.schemaXml = root.getString("schema-xml");
			this.solrConfigXml = root.getString("solr-config-xml");
		}

		@Override
		public String toString() {
			return "CoreConfiguration{" +
					"coreName='" + coreName + '\'' +
					", schemaXml='" + schemaXml + '\'' +
					", solrConfigXml='" + solrConfigXml + '\'' +
					'}';
		}
	}

	public static class EngineMatrix{
		final Map<Integer,String[]>		shardEngines;
		final Map<String,Integer>		engineShard;

		public EngineMatrix(List<JsonProperties> shards){
			ImmutableMap.Builder<Integer,String[]> shardEnginesBuilder = ImmutableMap.builder();
			ImmutableMap.Builder<String,Integer> engineShardBuilder = ImmutableMap.builder();
			Preconditions.checkArgument(shards.size() > 0,"Engine matrix shard size must be > 0");

			for(JsonProperties shard : shards){
				int shardId = shard.getInteger("shard-id");
				List<String> engines = shard.getStringArray("engines");

				shardEnginesBuilder.put(shardId,engines.toArray(new String[engines.size()]));
				for(String engine : engines){
					engineShardBuilder.put(engine.trim(),shardId);
				}
			}
			this.shardEngines = shardEnginesBuilder.build();
			this.engineShard = engineShardBuilder.build();
		}

		public String[] getEngines(int shardId){
			return shardEngines.get(shardId);
		}
		public Integer getShard(String engine){
			Preconditions.checkArgument(StringUtils.isNotBlank(engine),"Engine Host can not be null!");
			return engineShard.get(engine.trim());
		}

		@Override
		public String toString() {
			return "EngineMatrix{" +
					"shardEngines=" + shardEngines +
					", engineShard=" + engineShard +
					'}';
		}
	}


	public static void main(String[] args) throws URISyntaxException, IOException {
		InputStream inputStream = null;
		try{
			URL resource = Resources.getResource("engine-configuration.json");
			inputStream = resource.openStream();
			JsonProperties root = JsonPropertyUtils.loadProperty(inputStream);
			EngineConfiguration conf = new EngineConfiguration(root);
			System.out.println(conf);
		}finally {
			if(inputStream != null){
				inputStream.close();
			}
		}
	}


	@Override
	public String toString() {
		return "EngineConfiguration{" +
				"zookeeper='" + zookeeper + '\'' +
				", serviceName='" + serviceName + '\'' +
				", solrHome='" + solrHome + '\'' +
				", dataHome='" + dataHome + '\'' +
				", nettyPort=" + nettyPort +
				", httpPort=" + httpPort +
				", solrXml='" + solrXml + '\'' +
				", engineMatrix=" + engineMatrix +
				", coreConfigurations=" + Arrays.toString(coreConfigurations) +
				'}';
	}
}
