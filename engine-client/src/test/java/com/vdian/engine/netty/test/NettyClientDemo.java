//package com.vdian.engine.netty.test;
//
//import com.google.common.base.Stopwatch;
//import com.vdian.engine.client.netty.NettyLayout;
//import com.vdian.engine.client.netty.NettySolrClient;
//import com.vdian.engine.client.netty.RequestContext;
//import com.vdian.engine.client.netty.io.NettyResponse;
//import com.vdian.engine.client.netty.io.payload.SolrRequestPayload;
//import com.vdian.engine.client.netty.io.payload.SolrResponsePayload;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.HttpSolrClient;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.client.solrj.response.UpdateResponse;
//import org.apache.solr.common.SolrInputDocument;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.UnknownHostException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * User: xukun.fyp
// * Date: 16/12/19
// * Time: 18:05
// */
//public class NettyClientDemo {
//	private NettySolrClient		client;
//	private HttpSolrClient		httpClient;
//
//	@Before
//	public void init() throws UnknownHostException {
//		client = new NettySolrClient(NettyLayout.defaultNettyLayout("demo-test"));
//		client.init(new InetSocketAddress[]{localSocketAddress()});
//
//		httpClient = new HttpSolrClient("http://172.18.22.162:8080/hilbert");
//	}
//
//
//	@Test
//	public void batchQuery() throws InterruptedException {
//		ExecutorService es = Executors.newFixedThreadPool(15);
//		Stopwatch watch = Stopwatch.createStarted();
//		for(int i=0;i<100000;i++){
//			es.submit(new Runnable() {
//				@Override
//				public void run() {
//					try{
//						nettyQuery("title:新款");
////						httpQuery("title:新款");
//					}catch (Exception e){
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//		es.shutdown();
//		es.awaitTermination(10, TimeUnit.MINUTES);
//
//		System.out.println("Cost:" + watch.elapsed(TimeUnit.SECONDS));
//	}
//
//
//	final AtomicLong count = new AtomicLong(0);
//	volatile long start = System.currentTimeMillis();
//
////	@Test
//	public void nettyQuery(String q) throws IOException, InterruptedException {
//		SolrQuery query = new SolrQuery();
//		query.setQuery(q);
//		SolrRequestPayload payload = new SolrRequestPayload(query,"hilbert","/select");
//
//		RequestContext requestContext = new RequestContext(payload,localSocketAddress());
//		client.invokeOnce(requestContext);
//
//		count.addAndGet(1);
//		NettyResponse response = requestContext.getNettyResponse();
////		SolrResponsePayload responsePayload = (SolrResponsePayload)response.getPayload();
//		if(!response.isSuccess()){
//			System.out.println("code:" + ((SolrResponsePayload) response.getPayload()).getException().getCode());
//		}
//
//		if(System.currentTimeMillis() - start > 1000){
//			synchronized (this){
//				if(System.currentTimeMillis() - start > 1000){
//					start = System.currentTimeMillis();
//					System.out.println(count);
//					count.set(0);
//				}
//			}
//		}
//	}
//
////	@Test
//	public void httpQuery(String q) throws IOException, SolrServerException {
//		SolrQuery query = new SolrQuery();
//		query.setQuery(q);
//
//		QueryResponse response = httpClient.query(query);
////		System.out.println(response.getResults().getNumFound());
//		count.addAndGet(1);
//
//		if(System.currentTimeMillis() - start > 1000){
//			synchronized (this){
//				if(System.currentTimeMillis() - start > 1000){
//					start = System.currentTimeMillis();
//					System.out.println(count);
//					count.set(0);
//				}
//			}
//		}
//	}
//
//	public InetSocketAddress localSocketAddress() throws UnknownHostException {
//		InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(),8008);
////		System.out.println(address.getAddress().getHostAddress());
//		return address;
//	}
//
//
//
//
//
//
//	@Test
//	public void childUpdate() throws IOException, SolrServerException {
//		HttpSolrClient client = new HttpSolrClient("http://localhost:8080/");
//		SolrInputDocument document = new SolrInputDocument();
//		document.addField("item_id", 1000000020);
//
//		document.setField("price",666);
////		Map<String,Object> statusUpdate = new HashMap<>();
////		statusUpdate.put("set", 2);
////		document.addField("status", statusUpdate);
//
//
//		UpdateResponse response = client.add("hilbert", document, 1000);
//		System.out.println(response);
//	}
//
//}
