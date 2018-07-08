package com.morphism.search.netty.client;

import com.morphism.search.netty.common.ClientLayout;
import com.morphism.search.netty.common.RequestContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/4/5
 * Time: 19:17
 */
public class NettySolrClient extends SolrClient {
	private static NettyClient			defaultClient	=	new NettyClient(ClientLayout.defaultNettyLayout("default-netty-client"));
	private AtomicLong					refCount		=	new AtomicLong(0);
	private NettyClient					client;
	private InetSocketAddress			address;

	public NettySolrClient(){
		synchronized (defaultClient){
			if(client == null){
				client = defaultClient;
				refCount.incrementAndGet();
			}
		}
	}

	public NettySolrClient(NettyClient client){
		this.client = client;
	}

	public void resetAddress(InetSocketAddress address){
		this.address = address;
	}

	@Override
	public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
		RequestContext context = new RequestContext();
		context.reset(request, address, collection);
		this.client.invokeOnce(context);

		return context.getNamedList();
	}

	public void shutdown() {
		if(client != defaultClient){
			this.client.shutdown();
		}
	}

	@Override
	public void close() throws IOException {

	}
}
