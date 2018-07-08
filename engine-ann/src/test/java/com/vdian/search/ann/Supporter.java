package com.vdian.search.ann;

import com.google.common.collect.MinMaxPriorityQueue;
import com.vdian.search.ann.algorithm.FloatCalculator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 19:13
 */
public class Supporter {

	public static void print(String title, Pair... kvs) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n").append(title).append("\n");
		for (int i = 0; i < 80; i++) {
			builder.append("*");
		}
		for (Pair kv : kvs) {
			builder.append("\n");
			builder.append(String.format("%1$-20s:", kv.getKey())).append(kv.getValue());
		}
		System.out.println(builder.toString());
	}


	public static void printPair(Pair kv) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%1$-20s:", kv.getKey())).append(kv.getValue());
		System.out.println(builder.toString());
	}

	public static float[] randomVector(List<float[]> vectors){
		int idx = RandomUtils.nextInt(vectors.size());
		return vectors.get(idx);
	}

	public static List<float[]> nearestNeighbor(int num,float[] queryVector,List<float[]> vectors){
		List<float[]> result = new ArrayList<>(num);
		MinMaxPriorityQueue<PriorityElement<float[]>> mpq = MinMaxPriorityQueue.maximumSize(num).create();

		for(int i=0;i<vectors.size();i++){
			float distance = FloatCalculator.distance(queryVector, vectors.get(i));
			mpq.add(PriorityElement.of(distance, vectors.get(i)));
		}

		for(int i=0;i<num;i++){
			PriorityElement<float[]> element = mpq.pollFirst();
//			System.out.println(element.priority);
			result.add(element.node);
		}
		return result;
	}

	public static float accuracy(List<float[]> vectors,PriorityElement<Item>[] searchResults,float[] queryVector){
		List<float[]> srs = new ArrayList<>(searchResults.length);
		for(PriorityElement<Item> sr : searchResults){
			srs.add(sr.node.getVector());
		}
		return accuracy(vectors,srs,queryVector);
	}

	public static float accuracy(List<float[]> vectors,List<float[]> searchResults,float[] queryVector){
		int returnNum = searchResults.size();
		List<float[]> nns = nearestNeighbor(returnNum, queryVector, vectors);

		int intersect = 0;
		for(float[] sr : searchResults){
			for(float[] nn : nns){
				if(equal(nn,sr)){
					intersect++;
					break;
				}
			}
		}
		return (1.0f * intersect)/returnNum;
	}

	public static boolean equal(float[] f1,float[] f2){
		for(int i=0;i<f1.length;i++){
			if(f1[i] != f2[i]){
				return false;
			}
		}
		return true;
	}


	public static void deleteIfExist(String pathName) throws IOException {
		Path path = Paths.get(pathName);
		if(Files.isDirectory(path)){
			FileUtils.deleteDirectory(new File(pathName));
		}
	}
}
