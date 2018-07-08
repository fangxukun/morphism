//package com.vdian.engine.ann.lucene;
//
//import com.google.common.base.Preconditions;
//import com.google.common.base.Stopwatch;
//import com.vdian.engine.ann.*;
//import com.vdian.search.ann.AnnLayout;
//import com.vdian.search.ann.DataType;
//import org.apache.commons.lang3.tuple.Pair;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * User: xukun.fyp
// * Date: 17/3/9
// * Time: 17:40
// */
//public class AnnSearch {
//	public final String 	imageDataFile = "/Users/fangxukun/work/data/ann-lucene/image_vector.data";
//
//	int size = 100000;
//	int dimension = 40;
//	int returnNum = 10;
//	int searchNum = 10000;
//	int searchLoop = 5000;
//
//	int numOfTree = 80;
//	int leafItemNum = 64;
//	String field = "imgVector";
//	boolean rebuild = true;
//
//	IndexerLayout layout = new IndexerLayout(leafItemNum,200,numOfTree,"/Users/fangxukun/work/data/ann-search");
//	List<float[]> vectors;
//
//	@Test
//	public void testDataFormat() throws IOException {
//		List<float[]> fs = FilesReader.readRandomData(size,dimension);
//		Assert.assertEquals(fs.size(),size);
//		for(float[] f : fs){
//			Assert.assertEquals(f.length,dimension);;
//		}
//	}
//
//	@Before
//	public void init() throws IOException {
//		testDataFormat();
//		this.vectors = FilesReader.readRandomData(size, dimension);
//		Supporter.print("Ann搜索准确率与速度测试", Pair.of("向量数量", size), Pair.of("向量维数", dimension), Pair.of("搜索返回数量", returnNum), Pair.of("搜索数量", searchNum), Pair.of("AnnTree数量", numOfTree), Pair.of("Ann叶子节点数据量", leafItemNum));
//	}
//
//	@Test
//	public void runAll() throws Exception {
////		testMemory();
////		testDisk();
//		testLuceneSearch();
////		testExactSearch();
//	}
//
//	@Test
//	public void testMemory(){
//		MemoryIndexer indexer = new MemoryIndexer(layout);
//		for(int i=0;i<size;i++){
//			Item item = new Item(i,vectors.get(i));
//			indexer.addItem(item);
//		}
//		indexer.buildIndex(layout.numOfTree);
//
//		float accuracy = 0f;
//		Stopwatch sw = Stopwatch.createUnstarted();
//		for(int i=0;i<searchLoop;i++){
//			float[] queryVector = Supporter.randomVector(vectors);
//			sw.start();
//			PriorityElement<Item>[] searchResults = indexer.search(field, queryVector, returnNum, searchNum);
//			sw.stop();
//
//			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
//		}
//
//		Supporter.print("1.内存版检索速度与准确率", Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
//	}
//
//
//	@Test
//	public void testDisk() throws IOException {
//		if(rebuild || Files.notExists(Paths.get(layout.storePath))){
//			Supporter.deleteIfExist(layout.storePath);
//			CodecIndexer indexer = new CodecIndexer(layout,field);
//			for(int i=0;i<size;i++){
//				Item item = new Item(i,vectors.get(i));
//				indexer.addItem(item);
//			}
//			indexer.buildIndex(numOfTree);
//		}
//
//
//		CodecSearcher searcher = new CodecSearcher(Paths.get(layout.storePath));
//		float accuracy = 0f;
//		Stopwatch sw = Stopwatch.createUnstarted();
//		for(int i=0;i<searchLoop;i++){
//			float[] queryVector = Supporter.randomVector(vectors);
//			sw.start();
//			PriorityElement<Item>[] searchResults = searcher.search(field, queryVector, returnNum, searchNum);
//			sw.stop();
//
//			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
//		}
//
//		Supporter.print("2.磁盘版检索速度与准确率", Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
//	}
//
//	@Test
//	public void testLuceneSearch() throws Exception {
//		SolrAnnCore core = null;
//		AnnLayout.injectAnnLayout(new AnnLayout(80,200,128,null, DataType.BYTE));
//		if(rebuild){
//			core = new SolrAnnCore(rebuild);
//			for(int i=0;i<size;i++){
//				com.vdian.search.ann.Item item = new com.vdian.search.ann.Item(i,vectors.get(i));
//				core.addItem(item);
//			}
//			core.buildIndex(numOfTree);		//notwork
//		}else{
//			core = new SolrAnnCore(false);
//		}
//
//
//		float accuracy = 0f;
//		Stopwatch sw = Stopwatch.createUnstarted();
//		for(int i=0;i<searchLoop;i++){
//			float[] queryVector = Supporter.randomVector(vectors);
//			List<float[]> searchResults = core.search(queryVector, searchNum, returnNum,sw);
//			Preconditions.checkState(searchResults.size() == returnNum, "search size match!");
//			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
//		}
//
//		Supporter.print("3.Lucene-AnnFormat速度与准确率", Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
//	}
//
//	@Test
//	public void testExactSearch() throws Exception {
//		Stopwatch sw = Stopwatch.createUnstarted();
//		float accuracy = 0f;
//		for(int i=0;i<searchLoop;i++){
//			float[] queryVector = Supporter.randomVector(vectors);
//			sw.start();
//			List<float[]> searchResults = Supporter.nearestNeighbor(10,queryVector,vectors);
//			sw.stop();
//			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
//		}
//		Supporter.print("4.暴力搜索速度与准确率", Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
//	}
//}
