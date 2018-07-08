package com.morphism.search.ann;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.morphism.search.ann.lucene.AnnLayouts;
import com.morphism.search.ann.memory.AnnIndexer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 17/3/14
 * Time: 13:56
 */
public class AnnRandomBenchMark {
	int size 			= 10000;
	int dimension 		= 40;
	int returnNum 		= 10;
	int searchNum		= 1000;
	int searchLoop 		= 100;
	int meanIteration	= 200;

	int numOfTree 		= 80;
	int leafItemNum 	= 64;
	boolean diskIndex 	= false;

	List<float[]> 	vectors;
	AnnLayout		layout;

	@Before
	public void init() throws IOException {
		layout = new AnnLayout(numOfTree,meanIteration,leafItemNum,DataType.FLOAT,diskIndex);
		this.vectors = FilesReader.readRandomData(size, dimension);

//		this.vectors = FilesReader.readImgData("/Users/fangxukun/remote-data/tt.data",size,dimension);
		Supporter.print("Ann搜索准确率与速度测试", Pair.of("向量数量", size), Pair.of("向量维数", dimension), Pair.of("搜索返回数量", returnNum), Pair.of("搜索数量", searchNum), Pair.of("AnnTree数量", numOfTree), Pair.of("Ann叶子节点数据量", leafItemNum));
	}

	@Test
	public void runAll() throws Exception {
		testMemory();
		testLuceneFloatSearch();
		testLuceneShortSearch();
		testLuceneByteSearch();
		testExactSearch();
	}

	@Test
	public void testTime(){
		layout = new AnnLayout(2,40,8,DataType.FLOAT,diskIndex);
		size = 1000;

	}

	@Test
	public void testMemory(){
		Stopwatch indexSw = Stopwatch.createStarted();
		AnnIndexer indexer = new AnnIndexer(layout);
		for(int i=0;i<size;i++){
			indexer.addItem(vectors.get(i));
		}
		indexer.buildIndex();
		indexSw.stop();

		float accuracy = 0f;
		Stopwatch sw = Stopwatch.createUnstarted();
		for(int i=0;i<searchLoop;i++){
			float[] queryVector = Supporter.randomVector(vectors);
			sw.start();
			PriorityElement<Item>[] searchResults = indexer.search(queryVector,returnNum,searchNum);
			sw.stop();

			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
		}

		Supporter.print("1.内存版检索速度与准确率", Pair.of("索引时间(秒)", indexSw.elapsed(TimeUnit.SECONDS)), Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
	}


	@Test
	public void testLuceneFloatSearch() throws Exception {
		Stopwatch indexSw = Stopwatch.createStarted();

		//方便测试，使用AnnLayout注入，实际使用时可以在FieldType中配置参数。参见VectorField
		AnnLayout.injectAnnLayout(new AnnLayout(numOfTree, 200, leafItemNum, DataType.FLOAT,diskIndex));
		SolrAnnCore core = new SolrAnnCore(true);
		for(int i=0;i<size;i++){
			Item item = new Item(i,vectors.get(i));
			core.addItem(item);
		}
		core.buildIndex();		//notwork
		indexSw.stop();

		float accuracy = 0f;
		Stopwatch sw = Stopwatch.createUnstarted();
		for(int i=0;i<searchLoop;i++){
			float[] queryVector = Supporter.randomVector(vectors);
			List<float[]> searchResults = core.search(queryVector, searchNum, returnNum,sw);
			Preconditions.checkState(searchResults.size() == returnNum, "search size match!");
			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
		}

		core.close();
		Supporter.print("2.Lucene-AnnFormat-Float速度与准确率", Pair.of("索引时间(秒)", indexSw.elapsed(TimeUnit.SECONDS)), Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));


		AnnLayouts.clear();
	}


	@Test
	public void testLuceneShortSearch() throws Exception {
		Stopwatch indexSw = Stopwatch.createStarted();
		AnnLayout.injectAnnLayout(new AnnLayout(numOfTree, meanIteration, leafItemNum, DataType.SHORT,diskIndex));
		SolrAnnCore core = new SolrAnnCore(true);
		for(int i=0;i<size;i++){
			Item item = new Item(i,vectors.get(i));
			core.addItem(item);
		}
		core.buildIndex();		//notwork
		indexSw.stop();

		float accuracy = 0f;
		Stopwatch sw = Stopwatch.createUnstarted();
		for(int i=0;i<searchLoop;i++){
			float[] queryVector = Supporter.randomVector(vectors);
			List<float[]> searchResults = core.search(queryVector, searchNum, returnNum,sw);
			Preconditions.checkState(searchResults.size() == returnNum, "search size match!");
			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
		}

		core.close();
		Supporter.print("2.Lucene-AnnFormat-Short速度与准确率", Pair.of("索引时间(秒)", indexSw.elapsed(TimeUnit.SECONDS)), Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));

		AnnLayouts.clear();
	}

	@Test
	public void testLuceneByteSearch() throws Exception {
		Stopwatch indexSw = Stopwatch.createStarted();
		AnnLayout.injectAnnLayout(new AnnLayout(numOfTree, meanIteration, leafItemNum, DataType.BYTE,diskIndex));
		SolrAnnCore core = new SolrAnnCore(true);
		for(int i=0;i<size;i++){
			Item item = new Item(i,vectors.get(i));
			core.addItem(item);
		}
		core.buildIndex();
		indexSw.stop();

		float accuracy = 0f;
		Stopwatch sw = Stopwatch.createUnstarted();
		for(int i=0;i<searchLoop;i++){
			float[] queryVector = Supporter.randomVector(vectors);
			List<float[]> searchResults = core.search(queryVector, searchNum, returnNum,sw);
			Preconditions.checkState(searchResults.size() == returnNum, "search size match!");
			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
		}

		core.close();
		Supporter.print("3.Lucene-AnnFormat-Byte速度与准确率", Pair.of("索引时间(秒)", indexSw.elapsed(TimeUnit.SECONDS)), Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));

		AnnLayouts.clear();
	}

	@Test
	public void testExactSearch() throws Exception {
		Stopwatch sw = Stopwatch.createUnstarted();
		float accuracy = 0f;
		for(int i=0;i<searchLoop;i++){
			float[] queryVector = Supporter.randomVector(vectors);
			sw.start();
			List<float[]> searchResults = Supporter.nearestNeighbor(10, queryVector, vectors);
			sw.stop();
			accuracy += Supporter.accuracy(vectors,searchResults,queryVector);
		}
		Supporter.print("4.暴力搜索速度与准确率", Pair.of("平均耗时(微秒)", sw.elapsed(TimeUnit.MICROSECONDS) / searchLoop), Pair.of("准确率", 100 * accuracy / searchLoop));
	}

	@Test
	public void testMem(){
		System.out.println(Runtime.getRuntime().maxMemory()/(1024*1024));
		System.out.println(Runtime.getRuntime().freeMemory()/(1024*1024));
		System.out.println(Runtime.getRuntime().totalMemory() / (1024 * 1024));
	}
}
