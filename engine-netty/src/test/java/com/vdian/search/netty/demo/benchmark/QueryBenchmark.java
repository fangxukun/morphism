package com.vdian.search.netty.demo.benchmark;

import com.vdian.search.netty.common.NoResponseException;
import com.vdian.search.netty.server.bootstrap.FileBootstrap;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/4/5
 * Time: 16:32
 * Http,netty 性能测试
 */
public class QueryBenchmark {
	public static final String 			dataHome		=	"/data/data_home_http/";
	public static final int 			nettyPort		=	8006;

	@Test
	public void startNettyServer() throws IOException, InterruptedException {
		FileBootstrap nettyServer = newNettyServer();
		printHead("0.netty server start complete!");
		nettyServer.sync();
	}

	/**
	 * run search-engine/bin/run.sh first!
	 * @throws Exception
	 */
	@Test
	public void benchmark() throws Exception{
//		int addCount = 1000000;		//100w文档
		int addCount = 100000;
		int queryCount = 100000;
		boolean rebuild = false;

//		FileBootstrap nettyServer = null;
//		if(!Files.exists(Paths.get(dataHome)) || rebuild){
//			nettyServer = newNettyServer();
//			prepareData(addCount, nettyServer);
//			printHead("0. 文档准备完成,文档数量:" + addCount);
//		}else{
//			nettyServer = newNettyServer();
//			printHead("0. 文档已经存在,文档数量:" + addCount);
//		}

		int threadNum = 1;
		printHead("1. 1个查询线程,10w文档");
		nettyQuery(threadNum, queryCount);
		httpQueryNewClientPerThread(threadNum, queryCount);
		httpQueryNewClientEveryTime(threadNum, queryCount);

		printHead("2. 2个查询线程,10w文档");
		threadNum = 2;
		nettyQuery(threadNum,queryCount);
		httpQueryNewClientPerThread(threadNum,queryCount);
		httpQueryNewClientEveryTime(threadNum,queryCount);

		printHead("3. 3个查询线程,10w文档");
		threadNum = 3;

		httpQueryNewClientPerThread(threadNum, queryCount);
		nettyQuery(threadNum,queryCount);
		httpQueryNewClientEveryTime(threadNum,queryCount);


		printHead("4. 20个查询线程,10w文档");
		threadNum = 20;
		nettyQuery(threadNum,queryCount);
		httpQueryNewClientPerThread(threadNum,queryCount);
		httpQueryNewClientEveryTime(threadNum,queryCount);
	}


	public BenchContext addContext(int addCount){
		BenchContext context = new BenchContext();
		context.benchName = "addBench";
		context.bench.addCount = addCount/5;
		context.bench.threadNum = 5;
		context.bench.rebuild = true;
		return context;
	}

	public BenchContext queryContext(int threadNum,int totalQueryCount){
		BenchContext context = new BenchContext();
		context.benchName = "queryBench";
		context.bench.queryCount = totalQueryCount/threadNum;
		context.bench.threadNum = threadNum;
		context.bench.rebuild = false;
		return context;
	}

	public FileBootstrap newNettyServer() throws IOException{
		FileBootstrap server = new FileBootstrap();
		server.initConfiguration();
		server.init();
		return server;
	}

	public void prepareData(int addCount,FileBootstrap nettyServer) throws Exception{
		BenchContext context = addContext(addCount);

		try{
			HttpBenchmark http = new HttpBenchmark(context);
			http.addDocs();
		}catch (SolrServerException e){
			System.err.println("run search-engine/bin/run.sh first to start http server!");
			throw new RuntimeException("run search-engine/bin/run.sh first to start http server!");
		}
		System.out.println("init http engine data complete! addCount:" + addCount);

		NettyBenchmark netty = new NettyBenchmark(context,nettyPort);
		netty.addDocs();
		System.out.println("init netty engine data complete! addCount:" + addCount);
	}

	public void nettyQuery(int threadNum,int queryCount) throws IOException, InterruptedException, NoResponseException {
		BenchContext context = queryContext(threadNum,queryCount);
		context.benchName = "netty";
		NettyBenchmark netty = new NettyBenchmark(context,nettyPort);
		netty.queryDocs();
		System.out.println(context);
		netty.shutdown();
	}

	public void httpQueryNewClientEveryTime(int threadNum,int queryCount) throws IOException,InterruptedException{
		BenchContext context = queryContext(threadNum,queryCount);
		context.benchName = "Http-Client-PerQuery";
		HttpBenchmark http = new HttpBenchmark(context);
		http.queryDocs();
		System.out.println(context);
	}

	public void httpQueryNewClientPerThread(int threadNum,int queryCount) throws IOException,InterruptedException{
		BenchContext context = queryContext(threadNum,queryCount);
		context.benchName = "Http-Client-PerThread";
		HttpBenchmark http = new HttpBenchmark(context);
		http.queryDocsReuse();
		System.out.println(context);
	}

	public void printHead(String head){
		System.out.println();
		System.out.println();
		System.out.println(head);
		System.out.println("------------------------------------------------------------------------------------");
	}


}
