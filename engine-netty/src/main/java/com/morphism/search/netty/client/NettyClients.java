package com.morphism.search.netty.client;

import com.morphism.search.netty.common.NoResponseException;
import com.morphism.search.netty.common.RequestContext;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.SolrParams;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * User: xukun.fyp
 * Date: 17/4/5
 * Time: 19:29
 */
public class NettyClients {

	public static UpdateResponse commit(NettyClient client,InetSocketAddress address,String collection) throws IOException, NoResponseException, InterruptedException {
		final RequestContext context = client.allocateRequest();
		try{
			UpdateRequest request = new UpdateRequest();
			request.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
			context.reset(request, address, collection);
			client.invokeOnce(context);
			return context.getUpdateResponse();
		}finally {
			client.releaseRequest(context);
		}
	}

	public static UpdateResponse optimize(NettyClient client,InetSocketAddress address,String collection) throws IOException, NoResponseException, InterruptedException {
		final RequestContext context = client.allocateRequest();
		try{
			UpdateRequest request = new UpdateRequest();
			request.setAction(AbstractUpdateRequest.ACTION.OPTIMIZE, true, true);
			context.reset(request, address, collection);
			context.setTimeout(1000 * 180);
			client.invokeOnce(context);
			return context.getUpdateResponse();
		}finally {
			client.releaseRequest(context);
		}
	}

	public static QueryResponse query(NettyClient client,InetSocketAddress address,SolrParams params,String collection) throws InterruptedException, IOException, NoResponseException {
		final RequestContext context = client.allocateRequest();
		try{
			context.reset(new QueryRequest(params), address, collection);
			client.invokeOnce(context);
			return context.getQueryResponse();
		}finally {
			client.releaseRequest(context);
		}
	}
}
