package com.vdian.search.ann.lucene.codecs;

import com.vdian.search.ann.AnnLayout;
import com.vdian.search.ann.BinaryWritable;
import com.vdian.search.ann.DataType;
import com.vdian.search.ann.ScaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 12:41
 */
public class MetaCodec implements BinaryWritable{
	public List<FieldCodec>		fieldCodecs		=	new ArrayList<>();

	@Override
	public void readFields(IndexInput input) throws IOException {
		int length = input.readInt();

		for(int i=0;i<length;i++){
			FieldCodec fieldCodec = new FieldCodec();
			fieldCodec.readFields(input);
			fieldCodecs.add(fieldCodec);
		}
	}

	@Override
	public void write(IndexOutput output) throws IOException {
		output.writeInt(fieldCodecs.size());

		for(FieldCodec fieldCodec : fieldCodecs){
			fieldCodec.write(output);
		}
	}

	public FieldCodec addField(String fieldName,AnnLayout layout){
		FieldCodec field = new FieldCodec();
		field.fieldName = fieldName;
		field.dataType = layout.dataType;
		field.numOfTree = layout.numOfTree;

		field.treeCodecs = new TreeCodec[layout.numOfTree];
		for(int i=0;i<layout.numOfTree;i++){
			field.treeCodecs[i] = new TreeCodec();
		}

		fieldCodecs.add(field);
		return field;
	}

	public FieldCodec getField(String fieldName){
		for(FieldCodec fieldCodec : fieldCodecs){
			if(StringUtils.equals(fieldCodec.fieldName,fieldName)){
				return fieldCodec;
			}
		}
		throw new RuntimeException(String.format("field:%s can not found meta info!",fieldName));
	}


	public static class FieldCodec implements BinaryWritable{
		public String 			fieldName;			//not null
		public int				numOfTree;
		public DataType 		dataType;			//not null
		public int 				vectorSize;			//Vector size;

		public float[]			itemMinArray	=	new float[0];
		public float[]			itemMaxArray	=	new float[0];
		public float[]			itemInterval	=	new float[0];

		public TreeCodec[]		treeCodecs		=	new TreeCodec[0];

		@Override
		public void readFields(IndexInput input) throws IOException {
			fieldName = input.readString();
			numOfTree = input.readInt();
			dataType = DataType.fromCode(input.readByte());

			itemMinArray = readFloats(input);
			itemMaxArray = readFloats(input);
			vectorSize = itemMaxArray.length;
			itemInterval = ScaleUtils.interval(itemMinArray,itemMaxArray);

			treeCodecs = readBinaryArray(input,TreeCodec.class);
		}

		@Override
		public void write(IndexOutput output) throws IOException {
			output.writeString(fieldName);
			output.writeInt(numOfTree);
			output.writeByte(dataType.code);

			writeFloats(output, itemMinArray);
			writeFloats(output, itemMaxArray);

			writeBinaryArray(output,treeCodecs);
		}

		public void addTree(int index,long startFP,float[] minArray,float[] maxArray){
			treeCodecs[index].startFPOfTree = startFP;
			treeCodecs[index].treeMinArray = minArray;
			treeCodecs[index].treeMaxArray = maxArray;
		}
	}




	public static class TreeCodec implements BinaryWritable{
		public long				startFPOfTree;
		public float[]			treeMinArray		=	new float[0];
		public float[]			treeMaxArray		=	new float[0];
		public float[]			treeInterval		=	new float[0];

		@Override
		public void readFields(IndexInput input) throws IOException {
			startFPOfTree = input.readLong();
			treeMinArray = readFloats(input);
			treeMaxArray = readFloats(input);
			treeInterval = ScaleUtils.interval(treeMinArray,treeMaxArray);
		}

		@Override
		public void write(IndexOutput output) throws IOException {
			output.writeLong(startFPOfTree);
			writeFloats(output, treeMinArray);
			writeFloats(output,treeMaxArray);
		}
	}



	private static void writeBinaryArray(IndexOutput output,BinaryWritable[] values) throws IOException{
		output.writeInt(values.length);

		for(int i=0;i<values.length;i++){
			values[i].write(output);
		}
	}

	private static <T extends BinaryWritable> T[] readBinaryArray(IndexInput input,Class<T> clazz) throws  IOException{
		int length = input.readInt();
		T[] values = (T[])Array.newInstance(clazz,length);

		try{
			for(int i=0;i<length;i++){
				values[i] = clazz.newInstance();
				values[i].readFields(input);
			}
		}catch (Exception e){
			throw new RuntimeException(e);
		}
		return values;
	}


	private static void writeFloats(IndexOutput output,float[] values) throws IOException {
		output.writeInt(values.length);

		for(int i=0;i<values.length;i++){
			output.writeInt(Float.floatToIntBits(values[i]));
		}
	}

	private static float[] readFloats(IndexInput input) throws IOException{
		int length = input.readInt();
		float[] values = new float[length];

		for(int i=0;i<length;i++){
			values[i] = Float.intBitsToFloat(input.readInt());
		}
		return values;
	}
}
