package com.vdian.search.ann.lucene.codecs.floats;

import com.vdian.search.ann.AnnLayout;
import com.vdian.search.ann.BytesUtils;
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
public class AnnFloatTreeReader implements AnnTreeReader {
	private final IndexInput 			data;
	private final MetaCodec.FieldCodec 	field;
	private final AnnLayout 			layout;
	private final BinaryDocValues 		delegate;

	private final float[] 				floatBuffer;


	public AnnFloatTreeReader(IndexInput data, MetaCodec.FieldCodec field, AnnLayout layout, BinaryDocValues delegate) {
		this.data = data.clone();
		this.field = field;
		this.layout = layout;
		this.delegate = delegate;

		this.floatBuffer = new float[field.vectorSize];
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

			for (int i = 0; i < vectorLength; i++) {
				floatBuffer[i] = Float.intBitsToFloat(data.readInt());
			}
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


	@Override
	public BytesRef get(int docID) {
		return delegate.get(docID);
	}


	/**
	 * @param query
	 * @return
	 */
	@Override
	public Measurer createMeasurer(final float[] query) {
		return new Measurer() {
			@Override
			public float distance(int docId) {
				BytesRef ref = delegate.get(docId);
				float[] floats = BytesUtils.floatsFromBytes(ref.bytes);

				if (ref.length > 0) {
					float d = 0f;
					for (int i = 0; i < query.length; i++) {
						float diff = floats[i] - query[i];
						d += diff * diff;
					}
					return d;
				} else {
					return Float.MAX_VALUE;
				}
			}
		};
	}

	@Override
	public long ramBytesUsed() {
		return floatBuffer.length * RamUsageEstimator.NUM_BYTES_FLOAT;
	}

	@Override
	public Collection<Accountable> getChildResources() {
		return Collections.emptyList();
	}
}
