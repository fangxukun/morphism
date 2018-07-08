package com.vdian.search.ann.lucene.query;

import com.vdian.search.ann.lucene.codecs.AnnBinaryDocValues;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * User: xukun.fyp
 * Date: 17/3/8
 * Time: 18:25
 */
public class AnnVectorQuery extends Query {
	final String 			field;
	final float[]			queryVector;
	final int 				searchNum;

	public AnnVectorQuery(String filed, int searchNum, float[] queryVector){
		this.field = filed;
		this.queryVector = queryVector;
		this.searchNum = searchNum;
	}


	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
		return new ConstantScoreWeight(this) {
			@Override
			public Scorer scorer(LeafReaderContext context) throws IOException {
				LeafReader reader = context.reader();
				BinaryDocValues bdv = reader.getBinaryDocValues(field);

				if(bdv == null){
					return null;
				}
				if(!(bdv instanceof AnnBinaryDocValues)){
					throw new IllegalStateException(String.format("field %s was not indexed with AnnBinaryDocValue,bdv:" + bdv.getClass().getName()));
				}

				AnnBinaryDocValues adv = (AnnBinaryDocValues)bdv;
				DocIdSet result = adv.getReader().search(queryVector,searchNum);

				return new ConstantScoreScorer(this,score(),result.iterator());
			}
		};
	}


	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return super.rewrite(reader);
	}

	@Override
	public String toString(String field) {
		return "field:" + field;
	}

	@Override
	public boolean equals(Object o) {
		return sameClassAs(o);
	}

	@Override
	public int hashCode() {
		return classHash();
	}
}
