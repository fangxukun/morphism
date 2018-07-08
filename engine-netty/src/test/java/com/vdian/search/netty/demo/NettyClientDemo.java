package com.vdian.search.netty.demo;

import com.vdian.search.netty.client.NettyClient;
import com.vdian.search.netty.common.ClientLayout;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.search.netty.server.bootstrap.FileBootstrap;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 17/3/28
 * Time: 14:03
 */
public class NettyClientDemo {
	private NettyClient 			client;
	private InetSocketAddress		serverAddress;

	@Before
	public void setup(){
		ClientLayout layout = ClientLayout.defaultNettyLayout("clientDemo");

		this.client = new NettyClient(layout);
		this.serverAddress = new InetSocketAddress("localhost",8007);
	}

	@Test
	public void startServer() throws IOException, InterruptedException {
		FileBootstrap server = new FileBootstrap();
		server.initConfiguration("test");
		server.init();
		server.sync();
	}

	@Test
	public void addDocs() throws IOException{
		RequestContext requestContext = new RequestContext();

		for(int i=0;i<100;i++){
			UpdateRequest request = new UpdateRequest();
			request.add(newDocument(i));
			requestContext.reset(request, serverAddress, "vitem");
			client.invokeOnce(requestContext);
		}

		UpdateRequest commit = new UpdateRequest();
		commit.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		requestContext.reset(commit, serverAddress, "vitem");
		client.invokeOnce(requestContext);
	}

	@Test
	public void baseSearch() throws IOException{
		RequestContext context = new RequestContext();

		for(int i=0;i<100;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery("item_score:[0.2 TO 0.3]");
			query.setRows(10);
			query.setStart(0);
			QueryRequest request = new QueryRequest(query);
			context.reset(request, serverAddress, "vitem");

			client.invokeOnce(context);
		}
	}


	@Test
	public void concurrentRequest() throws InterruptedException {
		ExecutorService service = Executors.newCachedThreadPool();

		//add
		for(int i=0;i<5;i++){
			service.submit(new Runnable() {
				@Override
				public void run() {
					try{
						addDocs();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
		}

		//query
		for(int i=0;i<5;i++){
			service.submit(new Runnable() {
				@Override
				public void run() {
					try{
						baseSearch();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
		}
		service.shutdown();
		service.awaitTermination(2, TimeUnit.MINUTES);
	}

	private SolrInputDocument newDocument(int id){
		SolrInputDocument document = new SolrInputDocument();
		document.setField("item_id",id);
		document.setField("item_score", RandomUtils.nextDouble());
		return document;
	}
}
