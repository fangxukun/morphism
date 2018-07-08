package com.vdian.engine.server.cloud;

import com.google.common.base.Preconditions;
import com.vdian.engine.server.engine.recovery.TargetServer;
import com.vdian.search.commons.NetworkUtils;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.vmodel.core.ShardEntry;
import com.vdian.vmodel.core.ZookeeperClientFactory;
import com.vdian.vmodel.core.ZookeeperShardingModel;
import com.vdian.vmodel.node.ServerNode;
import com.vdian.vmodel.node.ShardNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * User: xukun.fyp
 * Date: 17/5/2
 * Time: 11:27
 */
public class VModelCloud {
	private static final Logger						LOGGER		= LoggerFactory.getLogger(VModelCloud.class);
	private final ZookeeperShardingModel			model;
	private final EngineConfiguration				configuration;
	private final ServerNode						serverNode;

	public VModelCloud(EngineConfiguration configuration){
		this.model = new ZookeeperShardingModel(configuration.serviceName,new ZookeeperClientFactory().setConnectString(configuration.zookeeper).setRetryPolicy(new ExponentialBackoffRetry(1000,3)));

		this.configuration = configuration;
		this.serverNode = serverNode();
	}

	public void init() throws Exception {
		this.model.start();

		ShardNode node = new ShardNode(serverNode.getShardId(),ShardNode.ShardStatus.Available);
		this.model.createShardNode(node);
		this.model.createOrUpdateServerNode(serverNode);
	}

	public void onlineWriter() throws Exception {
		this.model.createWriter(serverNode.hostKey());
		LOGGER.warn("onlineWriter " + NetworkUtils.localIp());
	}

	public void onlineReader() throws Exception{
		this.model.createReader(serverNode.hostKey());
		LOGGER.warn("onlineReader " + NetworkUtils.localIp());
	}

	public void offlineWriter() throws Exception{
		this.model.removeWriter(serverNode.hostKey());
	}

	public void offlineReader() throws Exception{
		this.model.removeReader(serverNode.hostKey());
	}

	public void offline() throws Exception{
		offlineWriter();
		offlineReader();

		this.model.removeServer(serverNode.hostKey());
	}

	public List<? extends ServerNode> availableReaders(){
		ShardEntry entry = model.getShardEntry(serverNode.getShardId());
		return entry.getAvailableReaders();
	}

	public Optional<TargetServer> peerServer(String coreName){
		List<? extends ServerNode> readers = availableReaders();
		if(readers.size() > 0){
			ServerNode node = readers.get(RandomUtils.nextInt(readers.size()));

			StringBuilder builder = new StringBuilder();
			builder.append("http://").append(node.getHost()).append(":").append(node.getHttpPort()).append("/").append(coreName);
			return Optional.of(new TargetServer(node.getHost(),builder.toString()));
		}
		return Optional.empty();
	}

	public String getDataPath(){
		return configuration.dataHome;
	}

	public String getTLogPath(){
		return Paths.get(configuration.dataHome,"tlogs").toString();
	}

	public String getTempPath(){
		return Paths.get(configuration.dataHome,".temp").toString();
	}


	private ServerNode serverNode(){
		String localIp = NetworkUtils.localIp();
		Integer shardId = configuration.engineMatrix.getShard(localIp);
		Preconditions.checkState(shardId != null,"current server:%s,is not found in shard,engineMatrix:%s",localIp,configuration.engineMatrix);

		ServerNode serverNode = new ServerNode(localIp,configuration.httpPort, StringUtils.EMPTY);
		serverNode.setHttpPort(configuration.httpPort);
		serverNode.setNettyPort(configuration.nettyPort);
		serverNode.setServiceStatus(ServerNode.ServiceStatus.Available);
		serverNode.setShardId(shardId);

		return serverNode;
	}


}
