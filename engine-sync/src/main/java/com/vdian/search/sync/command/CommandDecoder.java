package com.vdian.search.sync.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/8
 * Time: 14:19
 */
public class CommandDecoder extends ByteToMessageDecoder {
	public final static long				MAGIC_NUM		=	8872763533015872290L;
	private boolean 						cumulation		=	false;



	//ByteBuf in will release in ByteToMessageDecoder
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		if(in.readableBytes() < 8){
			ctx.fireChannelRead(in);
			return;
		}

		in.markReaderIndex();

		long magicNum = in.readLong();
		if(magicNum != MAGIC_NUM){
			in.resetReaderIndex();
			ctx.fireChannelRead(in);
		}else{
			int dataLength = in.readInt();
			if(in.readableBytes() < dataLength){
				in.resetReaderIndex();
				cumulation = true;
				return;
			}

			int commandId = in.readInt();
			Command command = Commands.createCommand(commandId);

			command.readFields(new ByteBufInputStream(in));
			out.add(command);
			cumulation = false;
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//没有cumulation,并且不是Command的消息，不做处理，丢给后面的Handler处理
		if(msg instanceof ByteBuf && cumulation == false){
			ByteBuf in = (ByteBuf)msg;

			if(in.readableBytes() < 8){
				ctx.fireChannelRead(msg);
				return;
			}

			in.markReaderIndex();
			long magicNum = in.readLong();
			in.resetReaderIndex();

			// 1/2^64 的概率会出现误判导致数据错误，平均传输16* 1G * 1G * 1024 = 16M PB 的数据会出错一次。
			if(magicNum != MAGIC_NUM ){
				//不是Command，不做处理
				ctx.fireChannelRead(msg);
				return;
			}
		}
		super.channelRead(ctx, msg);
	}
}
