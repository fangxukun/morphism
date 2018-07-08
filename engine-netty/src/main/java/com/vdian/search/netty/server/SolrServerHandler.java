package com.vdian.search.netty.server;

import com.vdian.search.netty.protocol.SolrProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 16/12/15
 * Time: 17:42
 */
public class SolrServerHandler extends SimpleChannelInboundHandler<SolrProtocol.NettyRequest> {
	private static final Logger			LOGGER		= LoggerFactory.getLogger(SolrServerHandler.class);
	private	final NettySolrEngine		engine;

	public SolrServerHandler(NettySolrEngine engine){
		this.engine = engine;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SolrProtocol.NettyRequest request) throws Exception {
		this.engine.request(ctx,request);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("Exception in ServerHandler",cause);

		System.err.println("Exception in ServerHandler");
		cause.printStackTrace();
		ctx.close();
	}
}
