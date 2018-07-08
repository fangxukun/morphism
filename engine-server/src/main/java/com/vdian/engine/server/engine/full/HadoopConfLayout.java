package com.vdian.engine.server.engine.full;

import com.koudai.rio.commons.properties.JsonProperties;
import org.apache.hadoop.conf.Configuration;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/25
 * Time: 16:21
 */
public class HadoopConfLayout {
	public final Configuration			configuration;

	public HadoopConfLayout(JsonProperties root){
		String configurationName = root.getString("configuration.name");

		switch (configurationName){
			case "direct":
				configuration = newDirectConf();
				break;
			case "proxy":
				configuration = newProxyConf();
				break;
			case "none":
				List<JsonProperties> props = root.getArray("configuration.entries");
				configuration = newNoneConf(props);
				break;
			default:
				throw new RuntimeException("configurationName:" + configurationName + " not supported! available names:[direct,proxy,none]");
		}
	}

	private Configuration newNoneConf(List<JsonProperties> props){
		Configuration conf = new Configuration();
		for(JsonProperties prop : props){
			String key = prop.getString("key");
			String val = prop.getString("value");
			configuration.set(key,val);
		}
		return conf;
	}

	private Configuration newProxyConf(){
		Configuration conf = newDirectConf();
		conf.set("hadoop.socks.server", "10.2.105.16:9001");
		conf.set("hadoop.rpc.socket.factory.class.default", "org.apache.hadoop.net.SocksSocketFactory");
		conf.set("dfs.client.use.legacy.blockreader", "true");
		return conf;
	}

	private Configuration newDirectConf(){
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://argo");
		conf.set("dfs.web.ugi", "hdfs,hadoop");
		conf.set("dfs.nameservices", "argo");
		conf.set("dfs.ha.namenodes.argo", "nn1,nn2");
		conf.set("dfs.namenode.rpc-address.argo.nn1", "idc02-argo-ds-00:9000");
		conf.set("dfs.namenode.rpc-address.argo.nn2", "idc02-argo-ds-01:9000");
		conf.set("dfs.client.failover.proxy.provider.argo", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
		return conf;
	}
}
