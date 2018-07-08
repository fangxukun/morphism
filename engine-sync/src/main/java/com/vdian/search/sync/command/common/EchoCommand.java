package com.vdian.search.sync.command.common;

import com.vdian.search.sync.command.Command;
import com.vdian.search.sync.command.Commands;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 09:04
 * 打印消息的Command.一些错误或者消息可以在另一边打印
 */
public class EchoCommand implements Command {
	private static final Logger		LOGGER	= LoggerFactory.getLogger(EchoCommand.class);
	public boolean 					success;
	public String 					message;


	@Override
	public int getCommandId() {
		return Commands.COMMAND_ECHO;
	}

	@Override
	public Command newInstance() {
		return new EchoCommand();
	}

	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeBoolean(success);
		out.writeUTF(message);
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		success = in.readBoolean();
		message = in.readUTF();
	}



	@Override
	public void handleCommand(ChannelHandlerContext ctx) {
		if(success){
			LOGGER.info(message);
		}else{
			LOGGER.error(message);
		}
	}
}
