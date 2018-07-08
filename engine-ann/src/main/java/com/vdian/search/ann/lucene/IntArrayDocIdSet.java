package com.vdian.search.ann.lucene;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: xukun.fyp
 * Date: 17/3/13
 * Time: 16:53
 */
public final class IntArrayDocIdSet extends DocIdSet {
	private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(IntArrayDocIdSet.class);

	private final int[] docs;
	private final int length;

	public IntArrayDocIdSet(int[] docs, int length) {
		if (docs[length] != DocIdSetIterator.NO_MORE_DOCS) {
			throw new IllegalArgumentException();
		}
		this.docs = docs;
		this.length = length;
	}

	@Override
	public long ramBytesUsed() {
		return BASE_RAM_BYTES_USED + RamUsageEstimator.sizeOf(docs);
	}

	@Override
	public DocIdSetIterator iterator() throws IOException {
		return new IntArrayDocIdSetIterator(docs, length);
	}

	static class IntArrayDocIdSetIterator extends DocIdSetIterator {

		private final int[] docs;
		private final int length;
		private int i = -1;
		private int doc = -1;

		IntArrayDocIdSetIterator(int[] docs, int length) {
			this.docs = docs;
			this.length = length;
		}

		@Override
		public int docID() {
			return doc;
		}

		@Override
		public int nextDoc() throws IOException {
			return doc = docs[++i];
		}

		@Override
		public int advance(int target) throws IOException {
			i = Arrays.binarySearch(docs, i + 1, length, target);
			if (i < 0) {
				i = -1 - i;
			}
			return doc = docs[i];
		}

		@Override
		public long cost() {
			return length;
		}

	}
}