package com.vdian.search.field.update.provider;

import com.vdian.search.field.update.TextTransformers;
import org.apache.lucene.util.BytesRef;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 11:20
 */
public class BinaryField {
	public int 									index;
	public String 								field;
	public TextTransformers.BinaryTransformer	transformer;

	public BinaryField(int index, String field,TextTransformers.BinaryTransformer transformer) {
		this.index = index;
		this.field = field;
		this.transformer = transformer;
	}
}
