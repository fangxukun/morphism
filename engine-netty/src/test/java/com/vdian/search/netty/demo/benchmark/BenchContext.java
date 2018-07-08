package com.vdian.search.netty.demo.benchmark;

import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/3/30
 * Time: 15:38
 */
public class BenchContext {
	public String 			benchName;
	public Bench			bench		=	new Bench();
	public Stat				stat		=	new Stat();

	private long 			start		=	System.currentTimeMillis();
	private long 			end			=	0;



	public static class Bench{
		public int 			addCount;
		public int 			queryCount;
		public boolean 		rebuild;
		public int 			threadNum;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			if(addCount > 0){
				builder.append("addCount:").append(addCount).append("\n")
						.append("threadNum:").append(threadNum).append("\n");
			}
			if(queryCount > 0){
				builder.append("queryCount:").append(queryCount).append("\n")
					   .append("threadNum:").append(threadNum);
			}
			builder.append("\n").append("rebuild:").append(rebuild);
			return builder.toString();
		}
	}


	public static class Stat{
		public AtomicLong 	addCount			=	new AtomicLong(0);
		public AtomicLong 	addFailedCount		=	new AtomicLong(0);
		public AtomicLong 	queryCount			=	new AtomicLong(0);
		public AtomicLong	queryFailedCount	=	new AtomicLong(0);


		public AtomicLong 	addCost		=	new AtomicLong(0);
		public AtomicLong	queryCost	=	new AtomicLong(0);
		public AtomicLong 	allNumFound	=	new AtomicLong(0);


		public Stat(){

		}

		public String toString(){
			StringBuilder builder = new StringBuilder();
			if(addCount.get() > 0){
				builder.append("addCount:").append(addCount).append("\n")
					   .append("addFailedCount:").append(addFailedCount).append("\n")
					   .append("addCost:").append(addCost).append("ms").append("\n")
					   .append("addAvg:").append(addCost.get() / Float.valueOf(addCount.get())).append("ms")
					   .append("\n");

			}
			if(queryCount.get() > 0){
				builder.append("queryCount:\t\t\t").append(queryCount).append("\n");

				if(queryFailedCount.get() > 0){
					builder.append("queryFailedCount:\t").append(queryFailedCount).append("\n");
				}
				builder.append("queryCost:\t\t\t").append(queryCost).append("ms").append("\n")
					   .append("query RT:\t\t\t").append(queryCost.get() / Float.valueOf(queryCount.get())).append("ms").append("\n")
					   .append("avgNumFound:\t\t").append(allNumFound.get()/queryCount.get()).append("\n");
			}

			return builder.toString();
		}
	}

	public void start(){
		this.start = System.currentTimeMillis();
	}

	public void end(){
		this.end = System.currentTimeMillis();
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append(benchName).append("\n");
		builder.append("----------------------------------------------------------------").append("\n");
//		builder.append(bench.toString()).append("\n");
//		builder.append("----------------------------------").append("\n");
		builder.append(stat.toString());

		long cost = end - start;
		builder.append("cost:\t\t\t\t" + cost).append("\n");
		builder.append("qps:\t\t\t\t" + bench.queryCount * bench.threadNum * 1000 / (float)cost);

		return builder.toString();
	}
}
