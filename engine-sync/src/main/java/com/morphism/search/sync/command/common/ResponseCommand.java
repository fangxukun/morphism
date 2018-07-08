package com.morphism.search.sync.command.common;

import com.morphism.search.sync.command.Command;
import com.morphism.search.sync.session.Tracker;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 13:41
 */
public abstract class ResponseCommand implements Command,Tracker {
	private long 	rid;

	public ResponseCommand() {
	}

	public ResponseCommand(long rid) {
		this.rid = rid;
	}

	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeLong(rid);
		write0(out);

		checkRid();
	}

	private void checkRid(){
		if(rid == -1){
			throw new IllegalStateException("rid must set in response(response.setRid(request.getRid))!");
		}
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		this.rid = in.readLong();
		readFields0(in);
	}

	public abstract void write0(ByteBufOutputStream out) throws IOException;
	public abstract void readFields0(ByteBufInputStream in) throws IOException;

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	@Override
	public void handleCommand(ChannelHandlerContext ctx) throws Exception {
		// just a response,noting to do
		throw new UnsupportedOperationException();
	}
}
