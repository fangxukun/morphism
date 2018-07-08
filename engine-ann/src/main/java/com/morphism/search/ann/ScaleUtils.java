package com.morphism.search.ann;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

/**
 * User: xukun.fyp
 * Date: 17/3/14
 * Time: 18:32
 */
public class ScaleUtils {

	public static Pair<float[],float[]> treeRange(Node tree){
		float[] treeMin = ScaleUtils.minInitValue(tree.vector.length);
		float[] treeMax = ScaleUtils.maxInitValue(tree.vector.length);

		minMaxNode(tree,treeMin,treeMax);
		return Pair.of(treeMin,treeMax);
	}

	public static void minMaxNode(Node node,float[] min,float[] max){
		if(node.left != null || node.right != null){
			rewrite(min,max,node.vector);
			minMaxNode(node.left,min,max);
			minMaxNode(node.right,min,max);
		}
	}

	public static float[] interval(float[] minArray,float[] maxArray){
		float[] interval = new float[minArray.length];
		for(int i=0;i<interval.length;i++){
			interval[i] = maxArray[i] - minArray[i];
		}
		return interval;
	}

	private static void rewrite(float[] min,float[] max,float[] val){
		for(int i=0;i<val.length;i++){
			min[i] = Math.min(min[i],val[i]);
			max[i] = Math.max(max[i],val[i]);
		}
	}


	public static float min(float[] array){
		float min = array[0];

		for(float item : array){
			min = Math.min(min, item);
		}
		return min;
	}

	public static float max(float[] array){
		float max = array[0];

		for(float item : array){
			max = Math.max(max, item);
		}
		return max;
	}



	public static void scaleFloatsToBytes(float[] src,float[] min,float[] interval,byte[] dest,int destOffset){
		for(int i=0;i<src.length;i++){
			dest[i + destOffset] = (byte)((src[i]-min[i])/interval[i] * 255 - 128);
		}
	}
	public static void scaleBytesToFloats(byte[] src,float[] min,float[] interval,float[] dest){
		for(int i=0;i<src.length;i++){
			dest[i] = min[i] + interval[i] * (src[i] + 128) / 255;
		}
	}
	public static float scaleByteToFloat(byte src,float min,float interval){
		return min + interval * (src + 128) / 255;
	}



	public static void scaleFloatsToShorts(float[] src,float[] min,float[] interval,byte[] dest,int destOffset){
		for(int i=0;i<src.length;i++){
			short shortVal = (short)((src[i]-min[i])/interval[i] * Short.MAX_VALUE);
			destOffset = BytesUtils.writeShort(dest,destOffset,shortVal);
		}
	}
	public static void scaleShortsToFloats(byte[] src,float[] min,float[] interval,float[] dest){
		for(int i=0;i<dest.length;i++){
			short shortVal = BytesUtils.readShort(src,i * 2);
			dest[i] = min[i] + interval[i] * shortVal / Short.MAX_VALUE;
		}
	}
	public static float scaleShortToFloat(byte[] src,int offset,float min,float interval){
		short shortVal = BytesUtils.readShort(src,offset);
		return min + interval * shortVal / Short.MAX_VALUE;
	}


	public static float[] minInitValue(int size){
		float[] val = new float[size];
		Arrays.fill(val,Float.POSITIVE_INFINITY);
		return val;
	}

	public static float[] maxInitValue(int size){
		float[] val = new float[size];
		Arrays.fill(val,Float.NEGATIVE_INFINITY);
		return val;
	}



}
