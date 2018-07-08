//package com.vdian.search.ann.lucene;
//
//import com.vdian.search.ann.BytesUtils;
//import com.vdian.search.ann.DataType;
//import com.vdian.search.ann.ScaleUtils;
//import org.apache.lucene.index.SegmentWriteState;
//import org.apache.lucene.store.IndexInput;
//import org.apache.lucene.store.IndexOutput;
//import org.apache.lucene.util.BytesRef;
//
//import java.io.IOException;
//
///**
// * User: xukun.fyp
// * Date: 17/3/15
// * Time: 17:50
// */
//public class AnnDiskIndexer {
//	private static final String 			MERGE_SUPPORT_FILE		=	"_ann_merge_temp";
//	private final Iterable<BytesRef>		values;
//	private final SegmentWriteState			state;
//
//	private BytesRef						buffer;
//	private float[]							min;				//所有Items每个维度的最小值
//	private float[]							max;
//	private int 							size;				//Items数量
//	private int								valueByteLength;	//Item中的Vector byte长度
//	private int								vectorLength;
//
//	private IndexInput						input;
//
//	public AnnDiskIndexer(Iterable<BytesRef> values, SegmentWriteState state) throws IOException{
//		this.values = values;
//		this.state = state;
//		this.size = 0;
//	}
//
//	public void init() throws IOException {
//		IndexOutput output = null;
//		try{
//			output = state.directory.createOutput(MERGE_SUPPORT_FILE,state.context);
//			for(BytesRef value : values){
//				if(valueByteLength == 0){
//					valueByteLength = value.length - value.offset;
//				}else{
//					assert valueByteLength == (value.length - value.offset);
//				}
//
//				float[] val = BytesUtils.floatsFromBytes(value.bytes);
//
//				if(vectorLength == 0){
//					vectorLength = val.length;
//				}else{
//					assert vectorLength == val.length;
//				}
//
//				if(min == null){
//					min = ScaleUtils.minInitValue(val.length);
//				}
//				if(max == null){
//					max = ScaleUtils.maxInitValue(val.length);
//				}
//
//				ScaleUtils.rewrite(min,max,val);
//
//				output.writeBytes(value.bytes, value.offset, value.length);
//				size++;
//			}
//			this.buffer = new BytesRef(new byte[valueByteLength]);
//		}finally {
//			if(output != null){
//				output.close();
//			}
//		}
//
//		input = state.directory.openInput(MERGE_SUPPORT_FILE,state.context);
//	}
//
//	public BytesRef readBytes(int docId) {
//		try{
//			this.input.seek(docId * valueByteLength);
//			this.input.readBytes(this.buffer.bytes, 0, valueByteLength);
//			return buffer;
//		}catch (IOException e){
//			throw new RuntimeException(e);
//		}
//	}
//
//	public void close() throws IOException{
//		this.input.close();
//		this.state.directory.deleteFile(MERGE_SUPPORT_FILE);
//	}
//
//	public DiskItemList createByteItemList(){
//		return new DiskItemList(this,DataType.BYTE);
//	}
//	public DiskItemList createFloatItemList(){
//		return new DiskItemList(this, DataType.FLOAT);
//	}
//
//	public float[] getMin() {
//		return min;
//	}
//
//	public float[] getMax() {
//		return max;
//	}
//
//	public int getSize() {
//		return size;
//	}
//
//	public int getVectorLength() {
//		return vectorLength;
//	}
//}
