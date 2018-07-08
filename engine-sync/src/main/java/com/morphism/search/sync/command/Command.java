package com.morphism.search.sync.command;

import io.netty.channel.ChannelHandlerContext;

/**
 * User: xukun.fyp
 * Date: 17/5/8
 * Time: 13:58
 */
public interface Command extends BinaryWritable{

	int getCommandId();

	Command newInstance();

	/**
	 * this method will execute on server side!
	 * @param ctx
	 * @throws Exception
	 */
	void handleCommand(ChannelHandlerContext ctx) throws Exception;

	default Commands.CommandFactory factory(){
		return ()->newInstance();
	}
}
