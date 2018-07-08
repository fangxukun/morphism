package com.morphism.search.netty.server;

import com.morphism.search.netty.protocol.RequestGetter;
import com.morphism.search.netty.protocol.ResponseSetter;
import com.morphism.search.netty.protocol.SolrProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.apache.solr.core.CoreContainer;

/**
 * User: xukun.fyp
 * Date: 17/3/16
 * Time: 16:04
 */
public class RequestWorker implements Runnable {
	private final NettySolrEngine 				engine;
	private final ResponseSetter 				responseSetter;
	private final RequestGetter 				requestGetter;
	private final SolrCoreDelegate 				delegate;

	private ChannelHandlerContext				ctx;


	public RequestWorker(CoreContainer container, NettySolrEngine engine){
		this.engine = engine;
		this.responseSetter = new ResponseSetter();
		this.requestGetter = new RequestGetter();

		this.delegate = new SolrCoreDelegate(container);
	}

	public void reset(SolrProtocol.NettyRequest request, ChannelHandlerContext ctx){
		this.ctx = ctx;
		this.requestGetter.reset(request);
		this.responseSetter.reset(requestGetter);
	}

	@Override
	public void run() {
		try{
			request();
		}catch (Exception e){
			responseSetter.setException(e);
		}finally {
			flushChannel();
			engine.release(this);
		}
	}

	private void request() throws Exception{
		this.delegate.request(requestGetter,responseSetter);
	}

	private void flushChannel(){
		SolrProtocol.NettyResponse response = responseSetter.build();
		ctx.writeAndFlush(response);
	}
}
