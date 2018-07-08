//package com.vdian.search.ann.lucene;
//
//import com.vdian.search.ann.*;
//import com.vdian.search.ann.algorithm.FloatCalculator;
//import org.apache.lucene.codecs.CodecUtil;
//import org.apache.lucene.codecs.DocValuesConsumer;
//import org.apache.lucene.index.FieldInfo;
//import org.apache.lucene.index.IndexFileNames;
//import org.apache.lucene.index.SegmentWriteState;
//import org.apache.lucene.store.IndexOutput;
//import org.apache.lucene.util.BytesRef;
//import org.apache.lucene.util.IOUtils;
//
//import java.io.Closeable;
//import java.io.IOException;
//import java.util.Iterator;
//
///**
// * User: xukun.fyp
// * Date: 17/3/12
// * Time: 15:31
// * 构建索引时，定义如何写入文件,
// * meta:存储多个字段，每个字段Ann索引的多个Tree的起始位置等。
// * data:存储Tree的节点信息。
// * 对于lucene版本，叶子节点挂的是docId,故对应的数据信息直接通过BinaryDocValue存储。
// */
//public class AnnDiskDocValuesConsumer extends DocValuesConsumer implements Closeable{
//	private final SegmentWriteState			state;
//	private final DocValuesConsumer			delegate;
//	private final AnnLayout					layout;
//
//	private IndexOutput						meta;
//	private IndexOutput						data;
//
//	public AnnDiskDocValuesConsumer(DocValuesConsumer delegate, SegmentWriteState state, AnnLayout layout) throws IOException{
//		this.delegate = delegate;
//		this.state = state;
//		this.layout = layout;
//
//		boolean success = false;
//		try{
//			String metaName = IndexFileNames.segmentFileName(state.segmentInfo.name, state.segmentSuffix, AnnDocValuesFormat.META_EXTENSION);
//			meta = state.directory.createOutput(metaName,state.context);
//			CodecUtil.writeIndexHeader(meta, AnnDocValuesFormat.META_CODEC,AnnDocValuesFormat.META_VERSION_CURRENT,state.segmentInfo.getId(),state.segmentSuffix);
//
//			String dataFile = IndexFileNames.segmentFileName(state.segmentInfo.name,state.segmentSuffix,AnnDocValuesFormat.DATA_EXTENSION);
//			data = state.directory.createOutput(dataFile,state.context);
//			CodecUtil.writeIndexHeader(data, AnnDocValuesFormat.DATA_CODEC,AnnDocValuesFormat.DATA_VERSION_CURRENT,state.segmentInfo.getId(),state.segmentSuffix);
//
//			success = true;
//		}finally {
//			if(!success){
//				IOUtils.closeWhileHandlingException(this);
//			}
//		}
//	}
//
//
//	@Override
//	public void addBinaryField(FieldInfo field, Iterable<BytesRef> values) throws IOException {
//		System.out.println("begin to addBinaryField disk!");
//		final AnnDiskIndexer dm = new AnnDiskIndexer(values,state);
//		dm.init();
//		try{
//			if(layout.dataType == DataType.BYTE){
//				addByteBinaryField(field,values,dm);
//			}else if(layout.dataType == DataType.FLOAT){
//				addFloatBinaryField(field, values,dm);
//			}
//		}finally {
//			dm.close();
//		}
//
//	}
//
//	private void addFloatBinaryField(FieldInfo field, Iterable<BytesRef> values,AnnDiskIndexer dm) throws IOException{
//		delegate.addBinaryField(field,values);
//
//		DiskItemList diskItems = dm.createFloatItemList();
//
//		meta.writeVInt(AnnDocValuesFormat.FIELD_FLAG);		//filed Flag
//		meta.writeString(field.name);						//field name
//		meta.writeByte(layout.dataType.code);				//filed data type (float,byte,int)
//
//		writeFloats(meta, dm.getMin());
//		writeFloats(meta, dm.getMax());
//
//		meta.writeVInt(layout.numOfTree);					//tree的数量
//		float[] treeMin = ScaleUtils.minInitValue(dm.getVectorLength());
//		float[] treeMax = ScaleUtils.maxInitValue(dm.getVectorLength());
//
//		for(int i=0;i<layout.numOfTree;i++){
//			System.out.println(String.format("write tree:%s,item-size:%s",i,diskItems.size()));
//			Node tree = FloatCalculator.makeTree(diskItems,layout);
//			long treeStartFP = writeNode(tree);
//			meta.writeVLong(treeStartFP);
//		}
//		writeFloats(meta, treeMin);
//		writeFloats(meta,treeMax);
//	}
//
//	private void addByteBinaryField(FieldInfo field, Iterable<BytesRef> values,final AnnDiskIndexer dm) throws IOException{
//		final Iterator<BytesRef> src = values.iterator();
//		final byte[] dest = new byte[dm.getVectorLength()];
//		delegate.addBinaryField(field, new Iterable<BytesRef>() {
//			@Override
//			public Iterator<BytesRef> iterator() {
//
//				return new Iterator<BytesRef>() {
//
//					@Override
//					public boolean hasNext() {
//						return src.hasNext();
//					}
//
//					@Override
//					public BytesRef next() {
//						BytesRef value = src.next();
//						float[] src = BytesUtils.floatsFromBytes(value.bytes);
//						ScaleUtils.scaleFloatsToBytes(src, dm.getMin(), dm.getMax(), dest,0);
//						byte[] bytes = BytesUtils.byteVectorToBytes(dest);
//						return new BytesRef(bytes, 0, bytes.length);
//					}
//
//					@Override
//					public void remove() {
//						src.remove();
//					}
//				};
//			}
//		});
//
//		DiskItemList diskItems = dm.createByteItemList();
//
//		meta.writeVInt(AnnDocValuesFormat.FIELD_FLAG);		//filed Flag
//		meta.writeString(field.name);						//field name
//		meta.writeByte(layout.dataType.code);                //filed data type (float,byte,int)
//
//		writeFloats(meta, dm.getMin());
//		writeFloats(meta, dm.getMax());
//
//		meta.writeVInt(layout.numOfTree);					//tree的数量
//
//		float[] treeMin = ScaleUtils.minInitValue(dm.getVectorLength());
//		float[] treeMax = ScaleUtils.maxInitValue(dm.getVectorLength());
//
//		for(int i=0;i<layout.numOfTree;i++){
//			System.out.println(String.format("write tree:%s,item-size:%s",i,diskItems.size()));
//			Node tree = FloatCalculator.makeTree(diskItems,layout);
//			ScaleUtils.minMaxNode(tree, treeMin, treeMax);
//			long treeStartFP = writeByteNode(tree, treeMin, treeMax);
//			meta.writeVLong(treeStartFP);
//		}
//		writeFloats(meta, treeMin);
//		writeFloats(meta,treeMax);
//
//	}
//
//	private void writeFloats(IndexOutput output,float[] val) throws IOException {
//		output.writeInt(val.length);
//		for(int i=0;i<val.length;i++){
//			meta.writeInt(Float.floatToIntBits(val[i]));
//		}
//	}
//
//	private long writeByteNode(Node node,float[] min,float[] max) throws IOException {
//		//1.left、right node
//		long leftStartFP = AnnDocValuesFormat.NONE;
//		if(node.left != null){
//			leftStartFP = writeByteNode(node.left,min,max);
//		}
//
//		long rightStartFP = AnnDocValuesFormat.NONE;
//		if(node.right != null){
//			rightStartFP = writeByteNode(node.right,min,max);
//		}
//
//		long startFP = data.getFilePointer();
//		data.writeVLong(leftStartFP);
//		data.writeVLong(rightStartFP);
//
//		//2.Vector
//		if(node.vector == null){
//			data.writeVInt(0);
//		}else{
//			data.writeVInt(node.vector.length);
//			byte[] dest = new byte[node.vector.length];
//			for(int i=0;i<node.vector.length;i++){
//				dest[i] = (byte)node.vector[i];
//			}
////			ScaleUtils.scaleFloatsToBytes(node.vector,min,max,dest);
//			data.writeBytes(dest,0,dest.length);
//		}
//
//		//3.PlaneOffset
//		data.writeInt(Float.floatToIntBits(node.planeOffset));
//
//		//4.Items
//		if(node.docIds == null){
//			data.writeVInt(0);;
//		}else{
//			data.writeVInt(node.docIds.length);
//			for(long docId : node.docIds){
//				data.writeVLong(docId);
//			}
//		}
//		return startFP;
//	}
//
//	private long writeNode(Node node) throws IOException {
//		//1.left、right node
//		long leftStartFP = AnnDocValuesFormat.NONE;
//		if(node.left != null){
//			leftStartFP = writeNode(node.left);
//		}
//
//		long rightStartFP = AnnDocValuesFormat.NONE;
//		if(node.right != null){
//			rightStartFP = writeNode(node.right);
//		}
//
//		long startFP = data.getFilePointer();
//		data.writeVLong(leftStartFP);
//		data.writeVLong(rightStartFP);
//
//		//2.Vector
//		if(node.vector == null){
//			data.writeVInt(0);
//		}else{
//			data.writeVInt(node.vector.length);
//			for(float f : node.vector){
//				data.writeInt(Float.floatToIntBits(f));
//			}
//		}
//
//		//3.PlaneOffset
//		data.writeInt(Float.floatToIntBits(node.planeOffset));
//
//		//4.Items
//		if(node.docIds == null){
//			data.writeVInt(0);;
//		}else{
//			data.writeVInt(node.docIds.length);
//			for(long docId : node.docIds){
//				data.writeVLong(docId);
//			}
//		}
//		return startFP;
//	}
//
//
//	@Override
//	public void addNumericField(FieldInfo field, Iterable<Number> values) throws IOException {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void addSortedField(FieldInfo field, Iterable<BytesRef> values, Iterable<Number> docToOrd) throws IOException {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void addSortedNumericField(FieldInfo field, Iterable<Number> docToValueCount, Iterable<Number> values) throws IOException {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void addSortedSetField(FieldInfo field, Iterable<BytesRef> values, Iterable<Number> docToOrdCount, Iterable<Number> ords) throws IOException {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void close() throws IOException {
//		boolean success = false;
//		try{
//			if(meta != null){
//				meta.writeVInt(-1);
//				CodecUtil.writeFooter(meta);
//			}
//			if(data != null){
//				CodecUtil.writeFooter(data);
//			}
//			success = true;
//		}finally {
//			if(success){
//				IOUtils.close(delegate,data,meta);
//			}else{
//				IOUtils.closeWhileHandlingException(delegate,data,meta);
//			}
//			meta = data = null;
//		}
//	}
//}
