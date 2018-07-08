//package com.vdian.search.ann.lucene;
//
//import com.vdian.search.ann.BytesUtils;
//import com.vdian.search.ann.DataType;
//import com.vdian.search.ann.Item;
//import com.vdian.search.ann.ScaleUtils;
//import org.apache.lucene.util.BytesRef;
//
//import java.util.ArrayList;
//
///**
// * User: xukun.fyp
// * Date: 17/3/15
// * Time: 18:13
// */
//public class DiskItemList extends ArrayList<Item> {
//	private final AnnDiskIndexer dm;
//	private final float[] 			min;
//	private final float[] 			max;
//	private final DataType			type;
//
//	public DiskItemList(AnnDiskIndexer diskMerger,DataType type) {
//		this.dm = diskMerger;
//		this.min = dm.getMin();
//		this.max = dm.getMax();
//		this.type = type;
//	}
//
//
//	@Override
//	public Item get(int index) {
//		if(type == DataType.BYTE){
//			return new ByteDiskItem(index, null);
//		}else if(type == DataType.FLOAT){
//			return new FloatDiskItem(index, null);
//		}
//		throw new RuntimeException("unknow data type!");
//	}
//
//	@Override
//	public int size() {
//		return dm.getSize();
//	}
//
//	public class ByteDiskItem extends Item {
//		public ByteDiskItem(long id, float[] vector) {
//			super(id, vector);
//		}
//
//		@Override
//		public float[] getVector() {
//			BytesRef ref = dm.readBytes((int) id);
//			float[] floats = BytesUtils.floatsFromBytes(ref.bytes);
//			ScaleUtils.scaleFloatsToBytes(floats, min, max);
//			return floats;
//		}
//	}
//
//	public class FloatDiskItem extends  Item{
//		public FloatDiskItem(long id, float[] vector) {
//			super(id, vector);
//		}
//
//		@Override
//		public float[] getVector() {
//			BytesRef ref = dm.readBytes((int) id);
//			float[] floats = BytesUtils.floatsFromBytes(ref.bytes);
//			return floats;
//		}
//	}
//}
