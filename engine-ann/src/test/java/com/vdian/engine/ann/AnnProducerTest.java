//package com.vdian.engine.ann;
//
//import com.vdian.engine.ann.codec.AnnProducer;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
///**
// * User: xukun.fyp
// * Date: 17/3/6
// * Time: 15:20
// */
//public class AnnProducerTest {
//
//	@Test
//	public void readTrees() throws IOException {
//		AnnProducer producer = new AnnProducer(Paths.get("/Users/fangxukun/work/data/ann-index"),"_1");
//		Node[] nodes = producer.readTrees("test");
//		for(Node node : nodes){
//			System.out.println(node.getVector());
//		}
//	}
//}
