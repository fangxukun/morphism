package com.vdian.search.ann.lucene.function;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 17:27
 */
public interface Measurer {
	float distance(int docId);
}
