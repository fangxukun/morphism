package com.morphism.search.netty.server;

import com.google.common.base.Preconditions;
import com.morphism.search.netty.client.ChannelHandler;
import com.morphism.search.netty.common.ServerLayout;
import com.morphism.search.netty.common.ThreadFactoryUtils;
import com.morphism.search.netty.protocol.SolrProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 16/12/13
 * Time: 14:36
 * Solr Netty服务端
 */
public class NettySolrServer {
	private static final Logger 			LOGGER = LoggerFactory.getLogger(NettySolrServer.class);
	private final ServerLayout 				nettyLayout;
	private final ServerBootstrap 			bootstrap;
	private final EventLoopGroup 			bossGroup;
	private final EventLoopGroup 			workGroup;
	private final NettySolrEngine 			engine;

	private Channel 						channel;


	public NettySolrServer(final ServerLayout nettyLayout, final CoreContainer container) {
		this.nettyLayout = nettyLayout;
		this.bossGroup = new NioEventLoopGroup(nettyLayout.bossThreadNum, ThreadFactoryUtils.newThreadFactory("netty-boss"));
		this.workGroup = new NioEventLoopGroup(nettyLayout.workThreadNum, ThreadFactoryUtils.newThreadFactory("netty-worker"));
		this.bootstrap = new ServerBootstrap();

		this.bootstrap.group(bossGroup, workGroup);
		this.bootstrap.channel(NioServerSocketChannel.class);
		this.bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator())
					  .childOption(ChannelOption.TCP_NODELAY, true)
					  .childOption(ChannelOption.SO_KEEPALIVE, true)
					  .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());

		this.engine = new NettySolrEngine(nettyLayout, container);
		this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(ChannelHandler.DECODER.name, new LengthFieldBasedFrameDecoder(nettyLayout.maxFrameLength, 0, 4, 0, 4));
				pipeline.addLast(ChannelHandler.PROTO_DECODER.name,new ProtobufDecoder(SolrProtocol.NettyRequest.getDefaultInstance()));

				pipeline.addLast(ChannelHandler.ENCODER.name, new LengthFieldPrepender(4));
				pipeline.addLast(ChannelHandler.PROTO_ENCODER.name,new ProtobufEncoder());

				pipeline.addLast(ChannelHandler.SOLR.name, new SolrServerHandler(engine));
			}
		});
	}


	public void start() {
		try {
			ChannelFuture future = this.bootstrap.bind(nettyLayout.port).sync();
			this.channel = future.channel();
			Preconditions.checkNotNull(channel, "NettyServer channel is null!");

			if (future.isSuccess()) {
				LOGGER.warn("NettyServer started! port:" + nettyLayout.port);
			}

		} catch (Exception e) {
			LOGGER.error("NettyServer start failed,port:" + nettyLayout.port, e);
			shutdown();
		}
	}


	public void sync() throws InterruptedException {
		this.channel.closeFuture().sync();
	}
	public void shutdown() {
		channel.close();
		workGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}


}
