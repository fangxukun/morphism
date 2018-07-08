//package com.vdian.engine.ann;
//
//import com.google.common.base.Stopwatch;
//import com.google.common.collect.MinMaxPriorityQueue;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// * User: xukun.fyp
// * Date: 17/3/3
// * Time: 11:05
// */
//public class AnnIndexerBm {
//
//	@Test
//	public void benchmark() throws IOException {
//		List<float[]> vectors = FilesReader.readFeatures(10000, 128);
//		IndexerLayout layout = new IndexerLayout(8, 200,"");        //这两个参数没啥影响
//		MemoryIndexer indexer = new MemoryIndexer(layout);
//		String field = "picture";
//
//		int numOfTree = 10, searchCount = 1000, returnNum = 10, searchNum = 500;
//
//		Stopwatch sw = Stopwatch.createStarted();
//		for (int i = 0; i < vectors.size(); i++) {
//			indexer.addItem(new Item(i, vectors.get(i)));
//		}
//		indexer.buildIndex(numOfTree);
//
//		print("1.索引构建", Pair.of("vectors:", vectors.size()), Pair.of("numOfTree", numOfTree), Pair.of("cost", sw.elapsed(TimeUnit.MILLISECONDS)));
//
//
//		//
//		sw.reset().start();
//		for (int i = 0; i < searchCount; i++) {
//			float[] queryVector = vectors.get(RandomUtils.nextInt(vectors.size()));
//			PriorityElement<Item>[] item = indexer.search(field, queryVector, returnNum, searchNum);
//		}
//		print("2.Ann检索", Pair.of("searchCount:", searchCount), Pair.of("returnNum", returnNum), Pair.of("searchNum", searchNum), Pair.of("cost", sw.elapsed(TimeUnit.MILLISECONDS)));
//
//		searchCount = 100;
//		sw.reset().start();
//		for (int i = 0; i < searchCount; i++) {
//			float[] queryVector = vectors.get(RandomUtils.nextInt(vectors.size()));
//			PriorityElement<Item>[] item = indexer.searchExact(field,queryVector, returnNum);
//		}
//		print("3.精确查找", Pair.of("searchCount:", searchCount), Pair.of("returnNum", returnNum), Pair.of("cost", sw.elapsed(TimeUnit.MILLISECONDS)));
//
//
//		int loop = 500;
//		float sumAccuracy = 0f;
//		for (int i = 0; i < loop; i++) {
//			float[] queryVector = vectors.get(RandomUtils.nextInt(vectors.size()));
//			PriorityElement<Item>[] annItems = indexer.search(field,queryVector, returnNum, searchNum);
//			PriorityElement<Item>[] exactItems = indexer.searchExact(field,queryVector, returnNum);
//			float accuracy = intersection(annItems, exactItems).size() / (annItems.length * 1.00f);
//			sumAccuracy += accuracy;
//		}
//
//		print("4.准确度", Pair.of("accuracy", sumAccuracy / (float) loop * 100), Pair.of("searchNum", searchNum), Pair.of("returnNum", returnNum));
//
//	}
//
//	private <T> List<T> intersection(T[] t1, T[] t2) {
//		List<T> result = new ArrayList<>();
//		Set<T> set1 = new HashSet<>(Arrays.asList(t1));
//		for (int i = 0; i < t2.length; i++) {
//			if (set1.contains(t2[i])) {
//				result.add(t2[i]);
//			}
//		}
//		return result;
//	}
//
//
//	private void print(String title, Pair... kvs) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("\n").append(title).append("\n");
//		for (int i = 0; i < 80; i++) {
//			builder.append("*");
//		}
//		for (Pair kv : kvs) {
//			builder.append("\n");
//			builder.append(String.format("%1$-20s:", kv.getKey())).append(kv.getValue());
//		}
//		System.out.println(builder.toString());
//	}
//
//	private void printPair(Pair kv) {
//		StringBuilder builder = new StringBuilder();
//		builder.append(String.format("%1$-20s:", kv.getKey())).append(kv.getValue());
//		System.out.println(builder.toString());
//	}
//
//
//	@Test
//	public void test() {
//		MinMaxPriorityQueue<PriorityElement<Item>> mpq = MinMaxPriorityQueue.orderedBy(new Comparator<PriorityElement<Item>>() {
//			@Override
//			public int compare(PriorityElement<Item> o1, PriorityElement<Item> o2) {
//				return o1.compareTo(o2);
//			}
//		}).maximumSize(10).create();
//
//		for (int i = 0; i < 100; i++) {
//		}
//
//		for (int i = 0; i < 10; i++) {
//			System.out.println(mpq.pollFirst().priority);
//		}
//	}
//
//
//	//对标annoy的examples/precision_test.py的测试
//	//目前耗时是annoy的两倍+,准确率相当
//	@Test
//	public void precisionTest(){
//		int f = 40,n = 100000;
//		String field = "picture";
//				IndexerLayout layout = new IndexerLayout(64,200,"/Users/fangxukun/work/data/ann-presion/128");
////		IndexerLayout layout = new IndexerLayout(64,200,"/Users/fangxukun/work/data/ann-presion/64");
//		MemoryIndexer indexer = new MemoryIndexer(layout);
//		for(int i=0;i<n;i++){
//			float[] v = new float[f];
//			for(int j=0;j<f;j++){
//				v[j] = RandomUtils.nextFloat();
//			}
//			indexer.addItem(new Item(i,v));
//		}
//
//		indexer.buildIndex(80);
//
//		int[] limits = {10000};
//		Stopwatch[] sws = {Stopwatch.createUnstarted(),Stopwatch.createUnstarted(),Stopwatch.createUnstarted(),Stopwatch.createUnstarted()};
//		int k = 10;
//		int searchNum = 1000;
//
//		Map<Integer,Float> precSum = new HashMap<>();
//		Map<Integer,Long> timeSum = new HashMap<>();
//		for(int limit : limits){
//			precSum.put(limit,0f);
//			timeSum.put(limit,0L);
//		}
//
//		for(int i=0;i<searchNum;i++){
//			float[] queryV = randomFloats(f);
//
//			PriorityElement<Item>[] eItems = indexer.searchExact(field,queryV, k);
//			for(int s=0;s<limits.length;s++){
//				sws[s].start();
//				PriorityElement<Item>[] annItems = indexer.search(field,queryV,k,limits[s]);
//				sws[s].stop();
//
//				int found = intersection(eItems,annItems).size();
//				float hitRate = 1.0f * found / k;
//
//				precSum.put(limits[s],precSum.get(limits[s]) + hitRate);
//			}
//		}
//
//		for(int i=0;i<limits.length; i++) {
//			print("", Pair.of("limit", limits[i]), Pair.of("precision", 100.0f * precSum.get(limits[i]) / searchNum), Pair.of("avgTime", sws[i].elapsed(TimeUnit.MICROSECONDS) / searchNum));
//		}
//	}
//
//	//对标annoy的examples/precision_test.py的测试
//	//目前耗时是annoy的两倍+,准确率相当
//	@Test
//	public void precisionTestFromDisk() throws IOException {
//		int f = 40,n = 100000;
//		String field = "picture";
////		IndexerLayout layout = new IndexerLayout(128,200,"/Users/fangxukun/work/data/ann-presion/128");
//		IndexerLayout layout = new IndexerLayout(256,200,"/Users/fangxukun/work/data/ann-presion/256");
////		CodecIndexer builder = new CodecIndexer(layout,field);
////
////		for(int i=0;i<n;i++){
////			float[] v = new float[f];
////			for(int j=0;j<f;j++){
////				v[j] = RandomUtils.nextFloat();
////			}
////			builder.addItem(new Item(i,v));
////		}
////
////		builder.buildIndex(80);
//
//
//
//		CodecSearcher searcher = new CodecSearcher(Paths.get(layout.storePath));
//
//		int[] limits = {10000};
//		Stopwatch[] sws = {Stopwatch.createUnstarted(),Stopwatch.createUnstarted(),Stopwatch.createUnstarted(),Stopwatch.createUnstarted()};
//		int k = 10;
//		int searchNum = 1000;
//
//		Map<Integer,Float> precSum = new HashMap<>();
//		Map<Integer,Long> timeSum = new HashMap<>();
//		for(int limit : limits){
//			precSum.put(limit,0f);
//			timeSum.put(limit,0L);
//		}
//
//		for(int i=0;i<searchNum;i++){
//			float[] queryV = randomFloats(f);
//			PriorityElement<Item>[] eItems = searcher.searchExact(field,queryV, k);
//			for(int s=0;s<limits.length;s++){
//				sws[s].start();
//				PriorityElement<Item>[] annItems = searcher.search(field,queryV,k,limits[s]);
//				sws[s].stop();
//
//				int found = intersection(eItems,annItems).size();
//				float hitRate = 1.0f * found / k;
//
//				precSum.put(limits[s],precSum.get(limits[s]) + hitRate);
//			}
//		}
//
//		for(int i=0;i<limits.length; i++) {
//			print("", Pair.of("limit", limits[i]), Pair.of("precision", 100.0f * precSum.get(limits[i]) / searchNum), Pair.of("avgTime", sws[i].elapsed(TimeUnit.MICROSECONDS)/searchNum));
//		}
//	}
//
//	private float[] randomFloats(int d){
//		float[] r = new float[d];
//		for(int i=0;i<d;i++){
//			r[i] = RandomUtils.nextFloat();
//		}
//		return r;
//	}
//}
