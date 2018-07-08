package com.vdian.search.netty.demo.benchmark;

import com.vdian.search.netty.client.NettyClient;
import com.vdian.search.netty.client.NettyClients;
import com.vdian.search.netty.common.ClientLayout;
import com.vdian.search.netty.common.NoResponseException;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.search.netty.server.bootstrap.FileBootstrap;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: xukun.fyp
 * Date: 17/3/29
 * Time: 13:19
 */
public class NettyBenchmark {

	private NettyClient client;
	private InetSocketAddress 			serverAddress;
	private String 						collection			=	"vitem";

	private BenchContext				benchContext;
	private BenchContext.Bench			bench;
	private BenchContext.Stat			stat;

	public NettyBenchmark(BenchContext context) throws IOException {
		FileBootstrap server = new FileBootstrap();
		server.initConfiguration();
		if (context.bench.rebuild) {
			if (Files.exists(Paths.get(server.getConfiguration().dataHome))) {
				FileUtils.deleteDirectory(new File(server.getConfiguration().dataHome));
			}
		}

		server.init();

		this.serverAddress = new InetSocketAddress("localhost", server.getPort());
		this.client = new NettyClient(ClientLayout.defaultNettyLayout("nettyBm"));

		this.benchContext = context;
		this.bench = context.bench;
		this.stat = context.stat;
	}

	public NettyBenchmark(BenchContext context,int port) throws IOException {
		this.serverAddress = new InetSocketAddress("localhost", port);
		this.client = new NettyClient(ClientLayout.defaultNettyLayout("nettyBm"));

		this.client.init(new InetSocketAddress[]{serverAddress});
		this.benchContext = context;
		this.bench = context.bench;
		this.stat = context.stat;
	}




	public void queryDocs() throws InterruptedException, IOException, NoResponseException {
		final CountDownLatch latch = new CountDownLatch(bench.threadNum);
		ExecutorService executor = Executors.newCachedThreadPool();

		this.benchContext.start();
		for (int i = 0; i < bench.threadNum; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						final RequestContext context = new RequestContext();
						long start =  System.currentTimeMillis();
						for (int i = 0; i < bench.queryCount; i++) {
							try {
								SolrQuery query = new SolrQuery();
								query.setQuery("item_title:高端大气");
								query.setRows(100);
								QueryRequest request = new QueryRequest(query);
								context.reset(request, serverAddress, collection);
								client.invokeOnce(context);

								if (context.isSuccess()) {
									QueryResponse response = context.getQueryResponse();
									stat.allNumFound.addAndGet(response.getResults().getNumFound());
									stat.queryCount.incrementAndGet();
								} else {
									stat.queryFailedCount.incrementAndGet();
								}

							} catch (Exception e) {
								stat.queryFailedCount.incrementAndGet();
								e.printStackTrace();
							}
						}
						stat.queryCost.addAndGet(System.currentTimeMillis() - start);
					} catch (Exception e) {
						System.out.println(stat);
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			});
		}

		latch.await();

		this.benchContext.end();
	}


	public void addDocs() throws InterruptedException, IOException, NoResponseException {
		final CountDownLatch latch = new CountDownLatch(bench.threadNum);
		ExecutorService executor = Executors.newCachedThreadPool();

		this.benchContext.start();
		for (int i = 0; i < bench.threadNum; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {

						final RequestContext context = new RequestContext();
						long start = System.currentTimeMillis();
						for (int i = 0; i < bench.addCount; i++) {
							try {
								SolrInputDocument document = DocumentGenerator.newDocument();
								UpdateRequest request = new UpdateRequest();
								request.add(document);
								context.reset(request, serverAddress, collection);
								client.invokeOnce(context);

								if (context.isSuccess()) {
									Assert.assertEquals(context.getUpdateResponse().getStatus(), 0);
									stat.addCount.incrementAndGet();
								} else {
									stat.addFailedCount.incrementAndGet();
								}

							} catch (Exception e) {
								System.err.println(Thread.currentThread() + "err!");
								throw new RuntimeException(e);
							}
						}
						stat.addCost.addAndGet(System.currentTimeMillis() - start);
					} catch (Throwable r) {
						r.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			});
		}

		latch.await();
		this.benchContext.end();
		this.commit();
		NettyClients.optimize(client, serverAddress, collection);
	}

	private void commit() throws IOException {
		final RequestContext context = new RequestContext();
		UpdateRequest request = new UpdateRequest();
		request.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		context.reset(request, serverAddress, collection);
		this.client.invokeOnce(context);
	}

	public void shutdown(){
		this.client.shutdown();
	}

}
