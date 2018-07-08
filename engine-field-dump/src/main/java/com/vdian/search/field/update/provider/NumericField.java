package com.vdian.search.field.update.provider;

import com.vdian.search.field.update.TextTransformers;
import org.apache.lucene.index.FieldInfo;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 11:20
 */
public class NumericField {
	public final int 									index;
	public final String 								field;
	public final TextTransformers.NumericTransformer	transformer;

	public NumericField(int index, String field,TextTransformers.NumericTransformer transformer) {
		this.index = index;
		this.field = field;
		this.transformer = transformer;

	}


}
