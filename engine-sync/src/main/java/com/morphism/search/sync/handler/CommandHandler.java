package com.morphism.search.sync.handler;

import com.morphism.search.sync.command.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 17/5/9
 * Time: 14:07
 */
public class CommandHandler extends SimpleChannelInboundHandler<Command>{
	private static final Logger LOGGER	= LoggerFactory.getLogger(CommandHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
		command.handleCommand(ctx);
	}


	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.warn("channel active!");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.warn("exception in server handler", cause);
	}
}
