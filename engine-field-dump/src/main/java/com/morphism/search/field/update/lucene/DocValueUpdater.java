package com.morphism.search.field.update.lucene;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 09:56
 */
public class DocValueUpdater {
	private final String 				keyField;
	private final String 				updateField;
	private final IndexWriter			indexWriter;

	private final BytesRefBuilder		builder;
	private final Term					keyTerm;

	public DocValueUpdater(String keyField, String updateField,IndexWriter indexWriter) {
		this.keyField = keyField;
		this.updateField = updateField;
		this.indexWriter = indexWriter;

		this.builder = new BytesRefBuilder();
		this.keyTerm = new Term(keyField,builder.get());
	}

	public void addNumericDocValues(long keyVal,long updateVal) throws IOException {
		resetLongKey(keyVal);
		indexWriter.updateNumericDocValue(keyTerm, updateField, updateVal);
	}

	public void addBinaryDocValues(long keyVal,BytesRef updateVal) throws IOException {
		resetLongKey(keyVal);
		indexWriter.updateBinaryDocValue(keyTerm, updateField, updateVal);
	}

	public void addBinaryDocValues(long keyVal,byte[] updateVal,int offset,int length) throws IOException {
		resetLongKey(keyVal);
		indexWriter.updateBinaryDocValue(keyTerm, updateField, new BytesRef(updateVal,offset,length));
	}

	public void commit() throws IOException {
		indexWriter.commit();
	}


	private void resetLongKey(long keyVal){
		this.builder.get().offset = 0;
		NumericUtils.longToPrefixCoded(keyVal, 0, builder);
	}

}
