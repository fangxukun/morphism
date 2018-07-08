package com.vdian.search.netty.demo.benchmark;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: xukun.fyp
 * Date: 17/3/30
 * Time: 17:09
 */
public class HttpBenchmark {

	private BenchContext				benchContext;
	private BenchContext.Bench			bench;
	private BenchContext.Stat			stat;

	public HttpBenchmark(BenchContext context){
		this.benchContext = context;
		this.bench = context.bench;
		this.stat = context.stat;
	}

	public void addDocs() throws IOException, InterruptedException, SolrServerException {
		final CountDownLatch latch = new CountDownLatch(bench.threadNum);
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < bench.threadNum; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						HttpSolrClient client = new HttpSolrClient("http://localhost:8080/vitem");
						long start = System.currentTimeMillis();
						for (int i = 0; i < bench.addCount; i++) {
							try {
								SolrInputDocument document = DocumentGenerator.newDocument();

								UpdateResponse response = client.add(document);

								if (response.getStatus() == 0) {
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
		HttpSolrClient client = new HttpSolrClient("http://localhost:8080/vitem");
		client.commit();
		client.optimize();
	}

	public void queryDocs() throws InterruptedException, IOException {
		final CountDownLatch latch = new CountDownLatch(bench.threadNum);
		ExecutorService executor = Executors.newCachedThreadPool();

		this.benchContext.start();
		for (int i = 0; i < bench.threadNum; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try{
						long start = System.currentTimeMillis();
						for (int i = 0; i < bench.queryCount; i++) {
							try {
//								HttpSolrClient client = new HttpSolrClient("http://172.19.37.226:8080/vitem");
								HttpSolrClient client = new HttpSolrClient("http://127.0.0.1:8080/vitem");
								SolrQuery query = new SolrQuery();
								query.setQuery("item_title:高端大气");
								query.setRows(100);
								QueryResponse response = client.query(query);

								if(response.getStatus() == 0){
									stat.queryCount.incrementAndGet();
									stat.allNumFound.addAndGet(response.getResults().getNumFound());
								}else{
									stat.queryFailedCount.incrementAndGet();
								}


							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						stat.queryCost.addAndGet(System.currentTimeMillis() - start);
					}finally {
						latch.countDown();
					}
				}
			});
		}

		latch.await();
		this.benchContext.end();
	}

	public void queryDocsReuse() throws InterruptedException, IOException {
		final CountDownLatch latch = new CountDownLatch(bench.threadNum);
		ExecutorService executor = Executors.newCachedThreadPool();

		this.benchContext.start();
		for (int i = 0; i < bench.threadNum; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try{
						HttpSolrClient client = new HttpSolrClient("http://localhost:8080/vitem");
						long start = System.currentTimeMillis();
						for (int i = 0; i < bench.queryCount; i++) {
							try {
								SolrQuery query = new SolrQuery();
								query.setQuery("item_title:高端大气");
								query.setRows(100);
								QueryResponse response = client.query(query);

								if(response.getStatus() == 0){
									stat.queryCount.incrementAndGet();
									stat.allNumFound.addAndGet(response.getResults().getNumFound());
								}else{
									stat.queryFailedCount.incrementAndGet();
								}


							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						stat.queryCost.addAndGet(System.currentTimeMillis() - start);
					}finally {
						latch.countDown();
					}
				}
			});
		}

		latch.await();
		this.benchContext.end();
	}

}
