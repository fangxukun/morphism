package com.vdian.search.ann.lucene.codecs;

import com.vdian.search.ann.lucene.function.Measurer;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 15:53
 */
public interface AnnTreeReader extends Accountable{

	DocIdSet search(float[] query,int searchNum) throws IOException;

	BytesRef get(int docID);

	Measurer createMeasurer(float[] query);
}
