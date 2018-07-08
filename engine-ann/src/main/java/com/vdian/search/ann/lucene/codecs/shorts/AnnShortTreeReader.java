package com.vdian.search.ann.lucene.codecs.shorts;

import com.vdian.search.ann.AnnLayout;
import com.vdian.search.ann.BytesUtils;
import com.vdian.search.ann.ScaleUtils;
import com.vdian.search.ann.lucene.IntArrayDocIdSet;
import com.vdian.search.ann.lucene.TLongPriorityQueue;
import com.vdian.search.ann.lucene.codecs.AnnTreeReader;
import com.vdian.search.ann.lucene.codecs.MetaCodec;
import com.vdian.search.ann.lucene.function.Measurer;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 15:55
 */
public class AnnShortTreeReader implements AnnTreeReader {
	private final IndexInput 				data;
	private final MetaCodec.FieldCodec 		field;
	private final AnnLayout 				layout;
	private final BinaryDocValues			delegate;

	private final byte[]					byteBuffer;
	private final float[]					floatBuffer;
	private final BytesRef					bytesRef;


	public AnnShortTreeReader(IndexInput data, MetaCodec.FieldCodec field, AnnLayout layout, BinaryDocValues delegate) {
		this.data = data.clone();
		this.field = field;
		this.layout = layout;
		this.delegate = delegate;

		this.byteBuffer = new byte[field.vectorSize * 2];
		this.floatBuffer = new float[field.vectorSize];
		this.bytesRef = new BytesRef(new byte[4 + field.vectorSize * 4]);
	}

	@Override
	public DocIdSet search(float[] query, int searchNum) throws IOException {
		TLongPriorityQueue queue = new TLongPriorityQueue(searchNum / layout.leafNodeMaxItem * 2);
		TIntHashSet docIds = new TIntHashSet(searchNum);

		for (int i = 0; i < field.numOfTree; i++) {
			queue.add(Float.MAX_VALUE, field.treeCodecs[i].startFPOfTree);
		}

		while (docIds.size() < searchNum && queue.size() > 0) {
			traceNode(queue.peekPriority(), queue.peekVal(), query, queue, docIds);
			queue.poll();
		}

		docIds.add(DocIdSetIterator.NO_MORE_DOCS);
		final int[] docs = docIds.toArray();
		Arrays.sort(docs);
		return new IntArrayDocIdSet(docs, docIds.size() - 1);
	}




	private void traceNode(float priority, long nodeStartFP, float[] qv, TLongPriorityQueue tpq, TIntSet docIdSet) throws IOException {
		data.seek(nodeStartFP);

		long leftStartFP = this.data.readVLong();
		long rightStartFP = this.data.readVLong();

		int vectorLength = this.data.readVInt();

		if (vectorLength != 0) {
			int treeId = this.data.readVInt();
			this.data.readBytes(byteBuffer, 0, vectorLength * 2);

			float[] minArray = field.treeCodecs[treeId].treeMinArray;
			float[] intervalArray = field.treeCodecs[treeId].treeInterval;

			//将存储的Short还原为Float
			ScaleUtils.scaleShortsToFloats(byteBuffer, minArray, intervalArray, floatBuffer);
		}

		float planeOffset = Float.intBitsToFloat(this.data.readInt());

		//4.Items
		int sizeOfItem = this.data.readVInt();
		if (sizeOfItem != 0) {
			for (int i = 0; i < sizeOfItem; i++) {
				docIdSet.add((int) data.readVLong());        //此处存储的都是docId，可以强转为int
			}
			return;
		}

		float margin = planeOffset;

		for (int k = 0; k < vectorLength; k++) {
			margin += qv[k] * floatBuffer[k];
		}

		tpq.add(Math.min(priority, margin), rightStartFP);
		tpq.add(Math.min(priority, -margin), leftStartFP);
	}


	/**
	 * 将存储的Short还原为Float，Merge的时候会读取此数据，并且没办法拿到tree里面的统计信息，
	 * 故只能在这里还原，存在一定的精度损失。
	 * @param docID
	 * @return
	 */
	@Override
	public BytesRef get(int docID) {
		BytesRef ref = delegate.get(docID);

		int length = BytesUtils.readInt(ref.bytes, 0);
		BytesUtils.writeInt(bytesRef.bytes,0,length);

		int offset = 4;
		float[] min = field.itemMinArray;
		float[] interval = field.itemInterval;
		for(int i=0;i<length;i++){
			float val = ScaleUtils.scaleShortToFloat(ref.bytes, i * 2 + 4, min[i], interval[i]);
			BytesUtils.writeInt(bytesRef.bytes,offset,Float.floatToIntBits(val));
			offset += 4;
		}

		return bytesRef;
	}




	/**
	 * distance(a,b) = sqrt((a1-b1)^2 + .... + (an-bn)^2)
	 * b:byte2Float=> min + interval * short / short.max
	 * b-a = min + interval/short.max * short - a
	 *     = (-a + min) + (interval/short.max) * short
	 *     = f1 + f2 * short
	 *
	 * @param query
	 * @return
	 */
	@Override
	public Measurer createMeasurer(final float[] query) {
		final float[] f1 = new float[query.length];
		final float[] f2 = new float[query.length];

		for(int i=0;i<query.length;i++){
			f1[i] = field.itemMinArray[i] - query[i];
			f2[i] = field.itemInterval[i] / Short.MAX_VALUE;
		}

		return new Measurer() {
			@Override
			public float distance(int docId) {
				BytesRef ref = delegate.get(docId);
				if(ref.length > 0){

					float d = 0f;
					for(int i=0;i<query.length;i++){
						short shortVal = BytesUtils.readShort(ref.bytes,i*2 +4);
						float diff = f1[i] + f2[i] * shortVal;
						d += diff * diff;
					}
					return d;
				}else{
					return Float.MAX_VALUE;
				}
			}
		};
	}

	@Override
	public long ramBytesUsed() {
		return byteBuffer.length +
				floatBuffer.length * RamUsageEstimator.NUM_BYTES_FLOAT +
				bytesRef.length;
	}

	@Override
	public Collection<Accountable> getChildResources() {
		return Collections.emptyList();
	}
}
