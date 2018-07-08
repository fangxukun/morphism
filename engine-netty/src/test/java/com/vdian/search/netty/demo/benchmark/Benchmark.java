package com.vdian.search.netty.demo.benchmark;

import org.junit.Test;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/30
 * Time: 16:09
 */
public class Benchmark {

	public BenchContext context(String name){
		BenchContext context = new BenchContext();
		context.benchName = name;
		context.bench.addCount = 100000;
		context.bench.queryCount = 1000;
		context.bench.rebuild = false;
		context.bench.threadNum = 1;
		return context;
	}


	@Test
	public void nettyBenchmark() throws Exception{
		BenchContext context = context("NettyBenchMark");
		NettyBenchmark netty = new NettyBenchmark(context);

		if(context.bench.rebuild) netty.addDocs();
		netty.queryDocs();
		print(context);
	}

	/**
	 * run search-engine/bin/run.sh first!
	 * @throws Exception
	 */
	@Test
	public void httpBenchmarkReuse() throws Exception{
		BenchContext context = context("HttpBenchMark");
		HttpBenchmark http = new HttpBenchmark(context);

		if(context.bench.rebuild) http.addDocs();
		http.queryDocsReuse();
		print(context);
	}

	@Test
	public void httpBenchmark() throws Exception{
		BenchContext context = context("HttpBenchMark");
		HttpBenchmark http = new HttpBenchmark(context);

		if(context.bench.rebuild) http.addDocs();
		http.queryDocs();
		print(context);
	}

	private void print(BenchContext context){
		System.out.println(context.toString());
	}
}
