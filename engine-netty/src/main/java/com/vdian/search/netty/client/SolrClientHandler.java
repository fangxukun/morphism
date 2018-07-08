package com.vdian.search.netty.client;

import com.vdian.search.netty.client.session.RequestSessionContext;
import com.vdian.search.netty.protocol.SolrProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * User: xukun.fyp
 * Date: 16/12/13
 * Time: 18:50
 * ThreadSafe guaranteed by netty
 */
public class SolrClientHandler extends SimpleChannelInboundHandler<SolrProtocol.NettyResponse> {
	private final RequestSessionContext 	sessionContext;


	public SolrClientHandler(RequestSessionContext sessionContext){
		this.sessionContext = sessionContext;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SolrProtocol.NettyResponse message) throws Exception {
		try{
			sessionContext.complete(message);
		}finally {
			ReferenceCountUtil.release(message);		//TODO:
		}
	}
}
