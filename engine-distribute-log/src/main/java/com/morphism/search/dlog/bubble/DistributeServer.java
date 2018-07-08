package com.morphism.search.dlog.bubble;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/6/8
 * Time: 15:38
 */
public class DistributeServer implements Closeable{
	public static final int 			PORT				= 7072;
	public static final int 			WORK_THREAD_NUM		= 4;
	public static final int 			MAX_FRAME_LENGTH	= 10 * 1024 * 1024;


	private static final Logger			LOGGER		= LoggerFactory.getLogger(DistributeServer.class);

	private final ServerBootstrap		bootstrap;
	private final EventLoopGroup		bossGroup;
	private final EventLoopGroup		workGroup;

	private Channel						channel;


	public DistributeServer(){
		this.bossGroup = new NioEventLoopGroup(1,
				new ThreadFactoryBuilder().setNameFormat("distribute-server-boss-%d")
										  .build());

		this.workGroup = new NioEventLoopGroup(WORK_THREAD_NUM,
				new ThreadFactoryBuilder().setNameFormat("distribute-server-worker-%d")
										  .build());

		this.bootstrap = new ServerBootstrap();
		this.bootstrap.group(bossGroup,workGroup);
		this.bootstrap.channel(NioServerSocketChannel.class);
		this.bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		this.bootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());

		this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH,0,4,0,4));
				pipeline.addLast(new LengthFieldPrepender(4));

				pipeline.addLast(new ServerHandler());
			}
		});
	}

	public void start() throws IOException {
		try{
			ChannelFuture future = this.bootstrap.bind(PORT).sync();
			this.channel = future.channel();
			Preconditions.checkNotNull(channel,"Distribute Server channel is null!");

			if(future.isSuccess()){
				LOGGER.warn("Distribute Server listening! port:" + PORT);
			}
		}catch (Exception e){
			LOGGER.error("Distribute Server start failed!,port:" + PORT,e);
			this.close();
		}
	}

	@Override
	public void close() throws IOException {
		if(this.channel != null){
			this.channel.close();
		}
		workGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}
}
