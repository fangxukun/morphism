package com.morphism.search.ann;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 12:49
 */
public interface BinaryWritable {

	void readFields(IndexInput input) throws IOException;

	void write(IndexOutput output) throws IOException;
}
