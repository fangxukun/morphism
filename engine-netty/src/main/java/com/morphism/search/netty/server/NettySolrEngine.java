package com.morphism.search.netty.server;

import com.morphism.search.netty.common.ServerLayout;
import com.morphism.search.netty.common.ThreadFactoryUtils;
import com.morphism.search.netty.protocol.SolrProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 16/12/16
 * Time: 15:38
 */
public class NettySolrEngine {
	private static final Logger							LOGGER			= LoggerFactory.getLogger(NettySolrEngine.class);
	private final CoreContainer							coreContainer;
	private final ExecutorService 						executor;
	private final ArrayBlockingQueue<RequestWorker>		freeWorkers;
	private final ServerLayout							nettyLayout;

	public NettySolrEngine(ServerLayout nettyLayout, CoreContainer coreContainer){
		LOGGER.warn("ClientLayout:" + nettyLayout);
		this.coreContainer = coreContainer;
		this.nettyLayout = nettyLayout;
		this.executor = Executors.newCachedThreadPool(ThreadFactoryUtils.newThreadFactory("engine-worker-thread"));

		this.freeWorkers = new ArrayBlockingQueue<RequestWorker>(nettyLayout.searchWorkerSize);

		for(int i=0;i<nettyLayout.searchWorkerSize;i++){
			this.freeWorkers.add(new RequestWorker(coreContainer,this));
		}
	}

	public void request(final ChannelHandlerContext context,final SolrProtocol.NettyRequest request) throws InterruptedException, IOException {
		RequestWorker worker = this.freeWorkers.poll(nettyLayout.workerPollTimeout,TimeUnit.MILLISECONDS);
		if(worker != null){
			worker.reset(request, context);
			executor.execute(worker);
		}else{
			System.out.println("worker is full!");
		}
	}

	public void release(RequestWorker worker){
		this.freeWorkers.add(worker);
	}

}
