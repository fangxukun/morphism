//package com.vdian.engine.ann;
//
//import com.google.common.base.Function;
//import com.google.common.base.Stopwatch;
//import com.google.common.collect.Lists;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.junit.Test;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * User: xukun.fyp
// * Date: 17/3/2
// * Time: 13:54
// */
//public class CalculatorBm {
//
//	@Test
//	public void distance() throws IOException {
//		int d = 512, loop = 300000;
//		List<float[]> vectors = FilesReader.readRandomData(loop, d);
//		List<int[]> intVectors = FilesReader.readRandomIntData(loop, d);
//		List<byte[]> byteVectors = FilesReader.readRandomByteData(loop, d);
//
//		Stopwatch sw1 = Stopwatch.createUnstarted();
//		Stopwatch sw2 = Stopwatch.createUnstarted();
//		Stopwatch sw3 = Stopwatch.createUnstarted();
//		Stopwatch sw4 = Stopwatch.createUnstarted();
//
//
//		for(int i=0;i<loop;i++){
//			sw3.start();
//			Calculator.distance(byteVectors.get(i),byteVectors.get(1));
//			sw3.stop();
//		}
//
//		for(int i=0;i<loop;i++){
//			sw1.start();
//			Calculator.distance(vectors.get(i), vectors.get(1));
//			sw1.stop();
//		}
//
//		for(int i=0;i<loop;i++){
//			sw2.start();
//			Calculator.distance(intVectors.get(i),intVectors.get(1));
//			sw2.stop();
//		}
//
//
//		print("1.向量距离计算性能:", Pair.of("dimension", d), Pair.of("loop", loop), Pair.of("distance", sw1.elapsed(TimeUnit.MICROSECONDS)), Pair.of("int distance", sw2.elapsed(TimeUnit.MICROSECONDS)),Pair.of("byte distance", sw3.elapsed(TimeUnit.MICROSECONDS)));
//	}
//
//
//	@Test
//	public void twoMeans() throws IOException {
//		int d=1000,numOfVector = 10000,step=10000;
//		List<float[]> vectors = FilesReader.readFeatures(numOfVector, d);
//		numOfVector = vectors.size();
//
//		List<Item> items = Lists.transform(vectors, new Function<float[], Item>() {
//			int i = 0;
//
//			@Override
//			public Item apply(float[] input) {
//				return new Item(i++, input);
//			}
//		});
//
//		Stopwatch sw = Stopwatch.createStarted();
//		Calculator.twoMeans(items, step);
//
//		print("2.计算两个中心点性能:", Pair.of("Vectors",numOfVector),Pair.of("Iterator-Step",step),Pair.of("Cost",sw.elapsed(TimeUnit.MILLISECONDS) + "ms"));
//	}
//
//
//
//
//	private void print(String title,Pair... kvs) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("\n").append(title).append("\n");
//		for(int i=0;i<80;i++){
//			builder.append("*");
//		}
//		for(Pair kv : kvs){
//			builder.append("\n");
//			builder.append(String.format("%1$-20s:",kv.getKey())).append(kv.getValue());
//		}
//		System.out.println(builder.toString());
//	}
//
//	private void random(float[] v) {
//		for (int i = 0; i < v.length; i++) {
//			v[i] = RandomUtils.nextFloat();
//		}
//	}
//}
