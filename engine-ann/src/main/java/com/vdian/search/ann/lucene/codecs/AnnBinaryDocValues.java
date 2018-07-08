package com.vdian.search.ann.lucene.codecs;

import com.vdian.search.ann.lucene.codecs.AnnTreeReader;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.util.BytesRef;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 16:05
 */
public class AnnBinaryDocValues extends BinaryDocValues {
	final AnnTreeReader reader;

	public AnnBinaryDocValues(AnnTreeReader reader) {
		this.reader = reader;
	}

	@Override
	public BytesRef get(int docID) {
		return reader.get(docID);
	}

	public AnnTreeReader getReader() {
		return reader;
	}

}
