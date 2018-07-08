package com.morphism.search.sync.handler;

import com.morphism.search.sync.command.common.ResponseCommand;
import com.morphism.search.sync.session.SessionContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 13:21
 */
public class CompleteHandler extends ChannelInboundHandlerAdapter{

	private final SessionContext			context;

	public CompleteHandler(SessionContext context) {
		this.context = context;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof ResponseCommand){
			context.complete((ResponseCommand)msg);
		}else{
			super.channelRead(ctx,msg);
		}
	}
}
