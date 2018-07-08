package com.morphism.search.ann.lucene.codecs.bytes;

import com.morphism.search.ann.AnnLayout;
import com.morphism.search.ann.BytesUtils;
import com.morphism.search.ann.Node;
import com.morphism.search.ann.ScaleUtils;
import com.morphism.search.ann.algorithm.FloatCalculator;
import com.morphism.search.ann.lucene.ValuesAnalyzer;
import com.morphism.search.ann.lucene.codecs.AnnDocValuesFormat;
import com.morphism.search.ann.lucene.codecs.AnnTreeWriter;
import com.morphism.search.ann.lucene.codecs.AnnDocValuesConsumer;
import com.morphism.search.ann.lucene.codecs.MetaCodec;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Iterator;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 14:43
 */
public class AnnByteTreeWriter implements AnnTreeWriter {
	protected final IndexOutput 			data;
	protected final IndexOutput				meta;
	protected final DocValuesConsumer 		delegate;
	protected final MetaCodec metaCodec;


	public AnnByteTreeWriter(AnnDocValuesConsumer consumer){
		this.data = consumer.data;
		this.meta = consumer.meta;
		this.delegate = consumer.delegate;
		this.metaCodec = consumer.metaCodec;
	}


	@Override
	public void addBinaryField(FieldInfo field, Iterable<BytesRef> values,AnnLayout layout) throws IOException {
		final ValuesAnalyzer analyzer = new ValuesAnalyzer(values);
		analyzer.analysis();

		writeDelegate(field, analyzer);

		MetaCodec.FieldCodec fieldCodec = metaCodec.addField(field.name,layout);
		fieldCodec.itemMinArray = analyzer.minArray;
		fieldCodec.itemMaxArray = analyzer.maxArray;

		for(int i=0;i<layout.numOfTree;i++){
			Node tree = FloatCalculator.makeTree(analyzer.items,layout);
			Pair<float[],float[]> minMax = ScaleUtils.treeRange(tree);
			float[] interval = ScaleUtils.interval(minMax.getLeft(),minMax.getRight());

			long treeStartFP = writeNode(i,tree,minMax.getLeft(),interval);
			fieldCodec.addTree(i,treeStartFP,minMax.getLeft(),minMax.getRight());
		}
	}


	private byte[]	buffer	= null;
	private long writeNode(int treeId,Node node,float[] minArray,float[] intervalArray) throws IOException{
		//1.left、right node
		long leftStartFP = AnnDocValuesFormat.NONE;
		if(node.left != null){
			leftStartFP = writeNode(treeId, node.left, minArray, intervalArray);
		}

		long rightStartFP = AnnDocValuesFormat.NONE;
		if(node.right != null){
			rightStartFP = writeNode(treeId, node.right, minArray, intervalArray);
		}

		long startFP = data.getFilePointer();
		data.writeVLong(leftStartFP);
		data.writeVLong(rightStartFP);

		//2.Vector
		if(node.vector == null){
			data.writeVInt(0);
		}else{
			data.writeVInt(node.vector.length);
			data.writeVInt(treeId);
			if(buffer == null){
				buffer = new byte[node.vector.length];
			}
			ScaleUtils.scaleFloatsToBytes(node.vector,minArray,intervalArray,buffer,0);
			data.writeBytes(buffer,0,buffer.length);
		}

		//3.PlaneOffset
		data.writeInt(Float.floatToIntBits(node.planeOffset));

		//4.Items
		if(node.docIds == null){
			data.writeVInt(0);;
		}else{
			data.writeVInt(node.docIds.length);
			for(long docId : node.docIds){
				data.writeVLong(docId);
			}
		}
		return startFP;
	}



	private void writeDelegate(final FieldInfo field,final ValuesAnalyzer analyzer) throws IOException{
		delegate.addBinaryField(field, new Iterable<BytesRef>() {
			@Override
			public Iterator<BytesRef> iterator() {
				return new Iterator<BytesRef>() {
					int i = 0;
					BytesRef buffer = null;

					@Override
					public boolean hasNext() {
						return i < analyzer.count;
					}

					@Override
					public BytesRef next() {
						// 将Float的结果每个维度Scale到Byte区间(0-127)
						float[] floatsVal = analyzer.getItem(i).getVector();
						if (buffer == null) {
							buffer = new BytesRef(new byte[floatsVal.length + 4]);
						}

						BytesUtils.writeInt(buffer.bytes, 0, floatsVal.length);
						ScaleUtils.scaleFloatsToBytes(floatsVal, analyzer.minArray, analyzer.intervalArray, buffer.bytes, 4);

						i++;
						return buffer;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		});
	}
}
