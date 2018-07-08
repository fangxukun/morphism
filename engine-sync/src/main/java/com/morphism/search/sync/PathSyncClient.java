package com.morphism.search.sync;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.morphism.search.sync.command.CommandDecoder;
import com.morphism.search.sync.command.CommandEncoder;
import com.morphism.search.sync.command.list.PathListRequest;
import com.morphism.search.sync.command.list.PathListResponse;
import com.morphism.search.sync.command.path.PathSyncCommand;
import com.morphism.search.sync.handler.ClientSyncHandler;
import com.morphism.search.sync.handler.CommandHandler;
import com.morphism.search.sync.handler.CompleteHandler;
import com.morphism.search.sync.session.SessionContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * User: xukun.fyp
 * Date: 17/5/4
 * Time: 16:11
 * Path(文件或者目录)同步客户端
 * 流程:{@link ClientSyncHandler}
 */
public class PathSyncClient {
	private static final Logger			LOGGER					= LoggerFactory.getLogger(PathSyncClient.class);
	private static final int 			DEFAULT_TRAFFIC_LIMIT	=	40;		//40M

	private Channel						channel;

	private final Bootstrap				bootstrap;
	private final EventLoopGroup		group;
	private final String 				host;

	private final SessionContext context;
	private final ClientSyncHandler		syncHandler;

	public PathSyncClient(String host){
		this(host, DEFAULT_TRAFFIC_LIMIT, true);
	}

	public PathSyncClient(String host,boolean closeOnSyncComplete){
		this(host, DEFAULT_TRAFFIC_LIMIT, closeOnSyncComplete);
	}

	/**
	 * @param host
	 * @param trafficLimitMB
	 * @param closeOnSyncComplete if false,call shutdown() when unused.
	 */
	public PathSyncClient(String host,int trafficLimitMB,boolean closeOnSyncComplete){
		this.host = host;
		this.group = new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("path-sync-netty-client").build());
		this.bootstrap = new Bootstrap();

		this.context = new SessionContext();
		this.syncHandler = new ClientSyncHandler(closeOnSyncComplete);

		trafficLimitMB = trafficLimitMB > 100 ? 100 : trafficLimitMB;
		final int safeTrafficLimitMB = trafficLimitMB <= 0 ? 1 : trafficLimitMB;

		this.bootstrap
				.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						long limit = safeTrafficLimitMB * 1024 * 1024;
						pipeline.addLast(new ChannelTrafficShapingHandler(limit, limit));
						pipeline.addLast(new CommandDecoder());
						pipeline.addLast(new CommandEncoder());
						pipeline.addLast(syncHandler);
						pipeline.addLast(new CompleteHandler(context));
						pipeline.addLast(new CommandHandler());
					}
				});
	}

	public void shutdown(){
		if(channel != null){
			this.channel.close();
		}

		this.group.shutdownGracefully();
	}

	/**
	 * 数据同步不支持并发处理。
	 * @param remotePath
	 * @param localPath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void syncPath(String remotePath,String localPath) throws IOException, InterruptedException {
		syncPath(Paths.get(remotePath), Paths.get(localPath));
	}

	public synchronized void syncPath(Path remotePath,Path localPath) throws IOException, InterruptedException {
		ensureChannel();

		PathSyncCommand listCommand = new PathSyncCommand();
		listCommand.fromPath = remotePath;
		listCommand.toPath = localPath;

		syncHandler.reset();
		channel.writeAndFlush(listCommand);
		syncHandler.waitComplete();
	}


	public boolean existPath(Path path) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		Path parent = path.getParent();
		String fileName = path.getFileName().toString();
		PathListResponse listResponse = listPath(parent.toString());
		return listResponse.getTPaths().stream().anyMatch((PathListResponse.TPath rPath) -> StringUtils.equals(rPath.getFileName(),fileName));
	}

	public PathListResponse listPath(String remotePath) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		ensureChannel();

		PathListRequest request = new PathListRequest(Paths.get(remotePath));
		long rid = context.allocateRid(request);
		channel.writeAndFlush(request);
		return context.waitComplete(rid);
	}


	private void ensureChannel() throws InterruptedException {
		if(this.channel == null){
			this.channel= bootstrap.connect(host, PathSyncServer.PORT).sync().channel();
		}
	}


}
