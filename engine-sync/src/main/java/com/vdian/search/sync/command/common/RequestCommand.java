package com.vdian.search.sync.command.common;

import com.vdian.search.sync.command.Command;
import com.vdian.search.sync.session.Tracker;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 13:38
 * 一个来回的命令
 */
public abstract class RequestCommand implements Command,Tracker{
	private long 		rid		=	-1;


	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeLong(rid);
		write0(out);
		checkRid();
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		this.rid = in.readLong();
		readFields0(in);
	}


	private void checkRid(){
		if(rid == -1){
			throw new IllegalStateException("rid must set in request!");
		}
	}
	public abstract void write0(ByteBufOutputStream out) throws IOException;
	public abstract void readFields0(ByteBufInputStream in) throws IOException;


	public void setRid(long rid) {
		this.rid = rid;
	}

	public long getRid() {
		return rid;
	}
}
