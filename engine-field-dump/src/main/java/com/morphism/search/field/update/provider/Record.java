package com.morphism.search.field.update.provider;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 11:14
 */
public interface Record {
	/**
	 * @return the term to identify the document
	 */
	Term getKeyTerm();

	/**
	 * @return all numeric field in the record
	 */
	String[] getNumericFields();


	long[] getNumericValues();

	/**
	 * @return all binary field in the record
	 */
	String[] getBinaryFields();


	BytesRef[] getBinaryValues();

}
