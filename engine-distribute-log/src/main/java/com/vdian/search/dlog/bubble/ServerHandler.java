package com.vdian.search.dlog.bubble;

import com.sun.org.apache.regexp.internal.RE;
import com.vdian.search.dlog.store.LogRecord;
import com.vdian.search.dlog.store.StoreDirectory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.solr.update.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * User: xukun.fyp
 * Date: 17/6/8
 * Time: 16:05
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger     LOGGER      = LoggerFactory.getLogger(ServerHandler.class);

	private StoreDirectory          dlogStore;
	private UpdateHandler           updateHandler;
	private LogRecord               logRecord;


	public ServerHandler(StoreDirectory dlogStore, UpdateHandler updateHandler) {
		this.dlogStore = dlogStore;
		this.updateHandler = updateHandler;
		this.logRecord = new LogRecord();
	}


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuffer source = (ByteBuffer)msg;
		logRecord.readFrom(source);
		dlogStore.write(logRecord);



	}


}
