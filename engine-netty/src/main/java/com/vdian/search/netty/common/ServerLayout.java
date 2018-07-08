package com.vdian.search.netty.common;

import com.koudai.rio.commons.properties.JsonProperties;

/**
 * User: xukun.fyp
 * Date: 16/12/15
 * Time: 17:16
 */
public class ServerLayout {
	public static final String 	NODE_NAME			=	"netty-server";

	public final int 			port;
	public final int 			bossThreadNum;
	public final int 			workThreadNum;
	public final int 			maxFrameLength;

	public final int 			searchWorkerSize;
	public final int 			workerPollTimeout;


	public ServerLayout(JsonProperties root){
		this.port = root.getInteger("port",8008);
		int processorNum = Math.max(Runtime.getRuntime().availableProcessors(),2);

		this.bossThreadNum = root.getInteger("boss-thread-num",1);
		this.workThreadNum = root.getInteger("work-thread-num",processorNum);
		this.maxFrameLength = root.getInteger("max-frame-length",10 * 1024 * 1024);

		this.searchWorkerSize = root.getInteger("search-worker-size",processorNum * 1000);
		this.workerPollTimeout = root.getInteger("worker-poll-timeout",300);
	}


	@Override
	public String toString() {
		return "ServerLayout{" +
				"port=" + port +
				", bossThreadNum=" + bossThreadNum +
				", workThreadNum=" + workThreadNum +
				", maxFrameLength=" + maxFrameLength +
				", searchWorkerSize=" + searchWorkerSize +
				", workerPollTimeout=" + workerPollTimeout +
				'}';
	}
}
