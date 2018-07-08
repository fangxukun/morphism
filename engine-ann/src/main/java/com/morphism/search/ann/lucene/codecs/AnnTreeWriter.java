package com.morphism.search.ann.lucene.codecs;

import com.morphism.search.ann.AnnLayout;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 14:24
 */
public interface AnnTreeWriter {
	static final float[]	EMPTY_FLOATS	=	new float[0];

	void addBinaryField(FieldInfo field,Iterable<BytesRef> values,AnnLayout layout) throws IOException;
}
