package com.morphism.engine.update.dv;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 16/12/3
 * Time: 13:13
 */
public class DocValueUpdater {
	private final IndexWriter					writer;

	public DocValueUpdater(IndexWriter writer){
		this.writer = writer;

	}

	public void updateNumericField(FieldInputIterator<Long,Number> fieldIterator) throws IOException {
		BytesRefBuilder bytesBuilder = new BytesRefBuilder();
		String keyField = fieldIterator.getKeyField();
		String valField = fieldIterator.getValField();

		while(fieldIterator.hasNext()){
			Pair<Long,Number> updateItem = fieldIterator.next();
			NumericUtils.longToPrefixCoded(updateItem.getKey(), 0, bytesBuilder);
			Term keyTerm = new Term(keyField,bytesBuilder.toBytesRef());
			writer.updateNumericDocValue(keyTerm,valField,updateItem.getValue().longValue());
		}
		writer.commit();
	}
}
