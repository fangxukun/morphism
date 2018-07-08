package com.vdian.search.sync.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/8
 * Time: 14:40
 * Command 转换为ByteBuf.
 * 1.插入MagicNum,用于标示是Command消息
 * 2.插入command的长度，方便Decoder解析避免出现解析的byte没有读全，一般刚开始读取1024byte. 另外也可以使用LengthFieldPrepender,不过可能存在copy
 */
public class CommandEncoder extends MessageToMessageEncoder<Command>{
	public static int 				COMMAND_MAX_CAPACITY		=	8192;			//如果命令长度大于8k，会抛出错误.

	@Override
	protected void encode(ChannelHandlerContext ctx, Command cmd, List<Object> out) throws Exception {
		final ByteBuf buf = ctx.alloc().buffer(COMMAND_MAX_CAPACITY);
		buf.writeLong(CommandDecoder.MAGIC_NUM);

		buf.markWriterIndex();
		buf.writeInt(0);
		int positionA = buf.writerIndex();

		buf.writeInt(cmd.getCommandId());
		try{
			cmd.write(new ByteBufOutputStream(buf));
		}catch (IndexOutOfBoundsException e){
			throw new RuntimeException("command exceed the max capacity:" + COMMAND_MAX_CAPACITY + ",Command:" + cmd.getClass().getName(),e);
		}

		int positionB = buf.writerIndex();
		int size = positionB - positionA;
		buf.resetWriterIndex();
		buf.writeInt(size);
		buf.writerIndex(positionB);

		out.add(buf);
	}

}
