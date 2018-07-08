//package com.vdian.engine.ann;
//
//import org.apache.commons.lang.math.RandomUtils;
//import org.junit.Test;
//
//import java.io.IOException;
//
///**
// * User: xukun.fyp
// * Date: 17/3/4
// * Time: 16:20
// */
//public class AnnConsumerTest {
//
//	@Test
//	public void testSave() throws IOException {
//		int f = 40, n = 100000;
//		CodecIndexer indexer = new CodecIndexer(IndexerLayout.defaultLayout(),"picture");
//
//		for (int i = 0; i < n; i++) {
//			float[] v = new float[f];
//			for (int j = 0; j < f; j++) {
//				v[j] = RandomUtils.nextFloat();
//			}
//			indexer.addItem(new Item(i, v));
//		}
//
//		indexer.buildIndex(10);
//	}
//}
