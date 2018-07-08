package com.morphism.search.sync;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.morphism.search.sync.command.CommandDecoder;
import com.morphism.search.sync.command.CommandEncoder;
import com.morphism.search.sync.handler.CommandHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 17/5/4
 * Time: 16:11
 * Path同步服务端。
 */
public class PathSyncServer {
	public static final int 			PORT 		=	7071;
	private static final Logger			LOGGER		= 	LoggerFactory.getLogger(PathSyncServer.class);
	private final ServerBootstrap		bootstrap;
	private final EventLoopGroup		group;

	private Channel						channel;

	public PathSyncServer(){
		this.group = new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("path-sync-netty-server").build());
		this.bootstrap = new ServerBootstrap();

		this.bootstrap.group(group)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new ChunkedWriteHandler());
						pipeline.addLast(new CommandDecoder());
						pipeline.addLast(new CommandEncoder());
						pipeline.addLast(new CommandHandler());
					}
				});

	}

	public void start(){
		try {
			ChannelFuture future = this.bootstrap.bind(PORT).sync();
			this.channel = future.channel();
			Preconditions.checkNotNull(channel, "NettyServer channel is null!");

			if (future.isSuccess()) {
				LOGGER.warn("PathSyncServer started! port:" + PORT);
			}

		} catch (Exception e) {
			LOGGER.error("PathSyncServer start failed,port:" + PORT, e);
			shutdown();
		}
	}

	/**
	 * Just for test
	 * @throws InterruptedException
	 */
	public void sync() throws InterruptedException {
		this.channel.closeFuture().sync();
	}

	public void shutdown() {
		if(channel != null){
			channel.close();
		}
		group.shutdownGracefully();
	}
}
