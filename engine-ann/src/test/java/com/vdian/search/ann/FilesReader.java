package com.vdian.search.ann;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/2
 * Time: 14:11
 */
public class FilesReader {
	public static List<float[]>	readFeatures(final int numOfVector,final int vectorDimension) throws IOException {
		return Resources.readLines(Resources.getResource("features.txt"), Charsets.UTF_8,new LineProcessor<List<float[]>>() {
			List<float[]> vectors = new ArrayList<float[]>();

			@Override
			public boolean processLine(String line) throws IOException {
				if(StringUtils.isNotBlank(line) && vectors.size() < numOfVector){
					String[] vs = StringUtils.split(line,"\t");
					float[] vector = new float[Math.min(vs.length,vectorDimension)];
					for(int i=0;i<vector.length;i++){
						vector[i] = Float.valueOf(vs[i]);
					}
					vectors.add(vector);
					return true;
				}else{
					return false;
				}

			}

			@Override
			public List<float[]> getResult() {
				return vectors;
			}
		});
	}

	public static List<float[]>	readImgData(String path,final int numOfVector,final int vectorDimension) throws IOException {
		return Files.readLines(new File(path), Charsets.UTF_8, new LineProcessor<List<float[]>>() {
			List<float[]> vectors = new ArrayList<>();
			@Override
			public boolean processLine(String line) throws IOException {
				if(StringUtils.isNotBlank(line) && vectors.size() < numOfVector){
					String[] fields = line.split("\u0001");
						String[] vs = fields[5].split(":");
						float[] vector = new float[Math.min(vs.length,vectorDimension)];
						for(int i=0;i<vector.length;i++){
							vector[i] = Float.valueOf(vs[i]);
						}
						vectors.add(vector);
					return true;
				}else{
					return false;
				}
			}

			@Override
			public List<float[]> getResult() {
				return vectors;
			}
		});
	}

	public static List<float[]> readRandomData(int numOfVector,int dimension){
		List<float[]> result = new ArrayList<>(numOfVector);
		for(int i=0;i<numOfVector;i++){
			float[] v = new float[dimension];
			for (int j=0;j<dimension;j++){
				v[j] = RandomUtils.nextFloat();
			}
			result.add(v);
		}
		return result;
	}

	public static List<int[]> readRandomIntData(int numOfVector,int dimension){
		List<int[]> result = new ArrayList<>(numOfVector);
		for(int i=0;i<numOfVector;i++){
			int[] v = new int[dimension];
			for (int j=0;j<dimension;j++){
				v[j] = RandomUtils.nextInt();
			}
			result.add(v);
		}
		return result;
	}

	public static List<byte[]> readRandomByteData(int numOfVector,int dimension){
		List<byte[]> result = new ArrayList<>(numOfVector);
		for(int i=0;i<numOfVector;i++){
			byte[] v = new byte[dimension];
			for (int j=0;j<dimension;j++){
				v[j] = (byte)RandomUtils.nextInt(Byte.MAX_VALUE);
			}
			result.add(v);
		}
		return result;
	}
}
