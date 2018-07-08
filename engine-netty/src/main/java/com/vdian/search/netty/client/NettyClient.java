package com.vdian.search.netty.client;

import com.google.common.base.Preconditions;
import com.vdian.search.netty.client.session.RequestSessionContext;
import com.vdian.search.netty.common.ClientLayout;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.search.netty.common.ThreadFactoryUtils;
import com.vdian.search.netty.protocol.SolrProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 16/12/13
 * Time: 15:29
 */
public class NettyClient {
	private static final long 					REQUEST_POOL_TIMEOUT	=	100;
	private static final Logger					LOGGER					= LoggerFactory.getLogger(NettyClient.class);
	private final ClientLayout 					clientLayout;
	private final Bootstrap						bootstrap;
	private final EventLoopGroup				workGroup;
	private final ChannelPoolContext			poolContext;
	private final RequestSessionContext 		sessionContext;
	private final ChannelPoolHandler			poolHandler;
	private final ObjectsPool<RequestContext>	contextPool;



	public NettyClient(final ClientLayout clientLayout){
		this.clientLayout = clientLayout;

		this.sessionContext = new RequestSessionContext();
		this.workGroup = new NioEventLoopGroup(clientLayout.workThreadNum, ThreadFactoryUtils.newThreadFactory("NettyClient-" + clientLayout.name));
		this.bootstrap = new Bootstrap();
		this.bootstrap.group(workGroup);
		this.bootstrap.channel(NioSocketChannel.class);
		this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
		this.bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
		this.bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));

		this.contextPool = new ObjectsPool<>(clientLayout.requestPoolSize, clientLayout.requestPoolSize, new ObjectsPool.Factory<RequestContext>() {
			@Override
			public RequestContext newInstance() {
				return new RequestContext();
			}
		});

		this.poolHandler = new ChannelPoolHandler() {
			@Override
			public void channelReleased(Channel ch) throws Exception {
			}

			@Override
			public void channelAcquired(Channel ch) throws Exception {
			}

			@Override
			public void channelCreated(Channel ch) throws Exception {
				LOGGER.warn("channel created:" + ch.remoteAddress());

				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(ChannelHandler.DECODER.name,new LengthFieldBasedFrameDecoder(clientLayout.maxFrameLength,0,4,0,4));
				pipeline.addLast(ChannelHandler.PROTO_DECODER.name,new ProtobufDecoder(SolrProtocol.NettyResponse.getDefaultInstance()));

				pipeline.addLast(ChannelHandler.ENCODER.name,new LengthFieldPrepender(4));
				pipeline.addLast(ChannelHandler.PROTO_ENCODER.name,new ProtobufEncoder());

				pipeline.addLast(ChannelHandler.SOLR.name,new SolrClientHandler(sessionContext));
			}
		};

		this.poolContext = new ChannelPoolContext(bootstrap, clientLayout.connectTimeout, clientLayout.toleranceFailedConnect,poolHandler);
	}


	public void init(InetSocketAddress[] remoteAddresses){
		poolContext.initPool(remoteAddresses);
	}

	public void shutdown(){
		this.poolContext.shutdown();
		this.workGroup.shutdownGracefully();
	}

	public Future<Boolean> invokeOnceAsync(RequestContext context) throws IOException {
		long rid = sessionContext.allocateRid(context);
		Channel channel = null;

		try{
			channel = poolContext.acquire(context.getRemoteAddress());
			channel.writeAndFlush(context.buildRequest());
			return sessionContext.future(rid);
		} finally {
//			ReferenceCountUtil.release(buffer);					//TODO:
			poolContext.release(context.getRemoteAddress(), channel);
		}
	}

	public Future<Boolean> invokeBatchAsync(RequestContext[] contexts) throws IOException{
		long rid = sessionContext.allocateRid(contexts);
		for(RequestContext context : contexts){
			Channel channel = null;

			try{
				channel = poolContext.acquire(context.getRemoteAddress());
				channel.writeAndFlush(context.buildRequest());
			}finally {
				poolContext.release(context.getRemoteAddress(),channel);
			}
		}
		return sessionContext.batchFuture(rid);
	}

	public void invokeOnce(RequestContext requestContext) throws IOException{
		try{
			Future<Boolean> future = invokeOnceAsync(requestContext);
			future.get(requestContext.getRequestTimeout(clientLayout.recallTimeout), TimeUnit.MILLISECONDS);
		}catch (Exception e){
			throw new IOException(e);
		}
	}

	public void invokeBatch(RequestContext[] requestContexts) throws IOException{
		try{
			long requestTimeout = requestContexts[0].getRequestTimeout(clientLayout.recallTimeout);
			Future<Boolean> future = invokeBatchAsync(requestContexts);
			future.get(requestTimeout, TimeUnit.MILLISECONDS);

			int successCount = 0;
			for(RequestContext rc : requestContexts){
				if(rc.isSuccess()){
					successCount++;
				}
			}

			Preconditions.checkState(successCount >= (requestContexts.length - clientLayout.toleranceFailedRequest), String.format("Tolerance Failed request exceed,successCount:%s,total:%s,tolerance:%s", successCount, requestContexts.length, clientLayout.toleranceFailedRequest));
		}catch (Exception e){
			throw new IOException(e);
		}
	}


	public RequestContext allocateRequest() throws InterruptedException {
		return this.contextPool.acquire(REQUEST_POOL_TIMEOUT,TimeUnit.MILLISECONDS);
	}

	public void releaseRequest(RequestContext context){
		if(context != null){
			this.contextPool.release(context);
		}
	}
}
