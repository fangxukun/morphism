package com.morphism.search.ann;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.solr.common.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * User: xukun.fyp
 * Date: 17/3/7
 * Time: 18:16
 */
public class BytesUtils {

	public static byte[] floatsToBytes(float[] vector){
		byte[] val = new byte[vector.length * 4 + 4];
		int offset = 0;
		offset = writeInt(val,offset,vector.length);
		for(int i=0;i<vector.length;i++){
			int fv = Float.floatToIntBits(vector[i]);
			offset = writeInt(val,offset,fv);
		}
		return val;
	}


	public static int writeInt(byte[] bytes,int offset,int val){
		bytes[offset] = (byte)(val >> 24);
		bytes[offset + 1] = (byte)(val >> 16);
		bytes[offset + 2] = (byte)(val >> 8);
		bytes[offset + 3] = (byte)val;
		return offset+4;
	}

	public static int writeShort(byte[] bytes,int offset,short val){
		bytes[offset] = (byte)(val>>8);
		bytes[offset + 1] = (byte)val;
		return offset + 2;
	}


	public static float[] floatsFromBytes(byte[] bytesVal){
		int offset = 0;
		int size = readInt(bytesVal,offset);
		offset+=4;
		float[] floatVal = new float[size];

		for(int i=0;i<size;i++){
			floatVal[i] = Float.intBitsToFloat(readInt(bytesVal,offset));
			offset += 4;
		}
		return floatVal;
	}


	public static int readInt(byte[] bytesVal,int offset){
		return bytesVal[offset] << 24 | (bytesVal[offset +1] &0xFF) << 16 | (bytesVal[offset +2] &0xFF) << 8 | (bytesVal[offset +3] &0xFF);
	}


	public static short readShort(byte[] bytes,int offset){
		return (short)((bytes[offset] << 8) | (bytes[offset+1]& 0xFF));
	}
}
