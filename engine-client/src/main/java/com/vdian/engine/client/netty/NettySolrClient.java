//package com.vdian.engine.client.netty;
//
//import com.google.common.base.Preconditions;
//import com.vdian.engine.client.netty.common.ThreadFactoryUtils;
//import com.vdian.engine.client.netty.session.RemoteSessionContext;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.PooledByteBufAllocator;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.pool.ChannelPoolHandler;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
//import io.netty.handler.codec.LengthFieldPrepender;
//import io.netty.handler.codec.protobuf.ProtobufDecoder;
//import io.netty.handler.codec.protobuf.ProtobufEncoder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//
///**
// * User: xukun.fyp
// * Date: 16/12/13
// * Time: 15:29
// */
//public class NettySolrClient {
//	private static final Logger				LOGGER		= LoggerFactory.getLogger(NettySolrClient.class);
//	private final NettyLayout 				nettyLayout;
//	private final Bootstrap					bootstrap;
//	private final EventLoopGroup			workGroup;
//	private final ChannelPoolContext		poolContext;
//	private final RemoteSessionContext 		sessionContext;
//	private final ChannelPoolHandler		poolHandler;
//
//
//	public NettySolrClient(final NettyLayout nettyLayout){
//		this.nettyLayout = nettyLayout;
//
//		this.sessionContext = new RemoteSessionContext();
//		this.workGroup = new NioEventLoopGroup(nettyLayout.workThreadNum, ThreadFactoryUtils.newThreadFactory("NettySolrClient-" + nettyLayout.name));
//		this.bootstrap = new Bootstrap();
//		this.bootstrap.group(workGroup);
//		this.bootstrap.channel(NioSocketChannel.class);
//		this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
//		this.bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
//		this.bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
//
//
//		this.poolHandler = new ChannelPoolHandler() {
//			@Override
//			public void channelReleased(Channel ch) throws Exception {
//			}
//
//			@Override
//			public void channelAcquired(Channel ch) throws Exception {
//			}
//
//			@Override
//			public void channelCreated(Channel ch) throws Exception {
//				LOGGER.warn("channel created:" + ch.remoteAddress());
//
//				ChannelPipeline pipeline = ch.pipeline();
//				pipeline.addLast(ChannelHandler.DECODER.name,new LengthFieldBasedFrameDecoder(nettyLayout.maxFrameLength,0,4,0,4));
////				pipeline.addLast(ChannelHandler.PROTO_DECODER.name,new ProtobufDecoder(SolrProtocol.SolrResponse.getDefaultInstance()));
//
//				pipeline.addLast(ChannelHandler.ENCODER.name,new LengthFieldPrepender(4));
//				pipeline.addLast(ChannelHandler.PROTO_ENCODER.name,new ProtobufEncoder());
//
//				pipeline.addLast(ChannelHandler.SOLR.name,new SolrClientHandler(sessionContext));
//			}
//		};
//
//		this.poolContext = new ChannelPoolContext(bootstrap,nettyLayout.connectTimeout,nettyLayout.toleranceFailedConnect,poolHandler);
//	}
//
//
//	public void init(InetSocketAddress[] remoteAddresses){
//		poolContext.initPool(remoteAddresses);
//	}
//
//	public void shutdown(){
//		this.poolContext.shutdown();
//		this.workGroup.shutdownGracefully();
//	}
//
//	public Future<Boolean> invokeOnceAsync(RequestContext requestContext) throws IOException {
//		long rid = sessionContext.allocateRid(requestContext);
//		Channel channel = null;
//		ByteBuf buffer = null;
//
//		try{
//			channel = poolContext.acquire(requestContext.getRemoteAddress());
//
//			buffer = channel.alloc().buffer();
//			requestContext.writeRequestBuffer(buffer);
//			ChannelFuture future = channel.writeAndFlush(buffer);
//			return sessionContext.future(rid);
//		} finally {
////			ReferenceCountUtil.release(buffer);
//			poolContext.release(requestContext.getRemoteAddress(), channel);
//		}
//	}
//
//	public Future<Boolean> invokeBatchAsync(RequestContext[] requestContexts) throws IOException{
//		long rid = sessionContext.allocateRid(requestContexts);
//		for(RequestContext requestContext : requestContexts){
//			Channel channel = null;
//			ByteBuf buffer = null;
//			try{
//				channel = poolContext.acquire(requestContext.getRemoteAddress());
//
//				buffer = channel.alloc().buffer();
//				requestContext.writeRequestBuffer(buffer);
//				channel.writeAndFlush(buffer);
//			}finally {
////				ReferenceCountUtil.release(buffer);
//				poolContext.release(requestContext.getRemoteAddress(),channel);
//			}
//		}
//		return sessionContext.batchFuture(rid);
//	}
//
//	public void invokeOnce(RequestContext requestContext) throws IOException{
//		try{
//			Future<Boolean> future = invokeOnceAsync(requestContext);
//			future.get(requestContext.getRequestTimeout(nettyLayout.recallTimeout), TimeUnit.MILLISECONDS);
//		}catch (Exception e){
//			throw new IOException(e);
//		}
//	}
//
//	public void invokeBatch(RequestContext[] requestContexts) throws IOException{
//		try{
//			long requestTimeout = requestContexts[0].getRequestTimeout(nettyLayout.recallTimeout);
//			Future<Boolean> future = invokeBatchAsync(requestContexts);
//			future.get(requestTimeout, TimeUnit.MILLISECONDS);
//
//
//			int successCount = 0;
//			for(RequestContext rc : requestContexts){
//				if(rc.isSuccess()){
//					successCount++;
//				}
//			}
//
//			Preconditions.checkState(successCount >= (requestContexts.length - nettyLayout.toleranceFailedRequest),
//					String.format("Tolerance Failed request exceed,successCount:%s,total:%s,tolerance:%s",successCount,requestContexts.length,nettyLayout.toleranceFailedRequest));
//		}catch (Exception e){
//			throw new IOException(e);
//		}
//	}
//}
