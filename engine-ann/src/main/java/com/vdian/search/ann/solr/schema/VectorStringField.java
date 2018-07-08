package com.vdian.search.ann.solr.schema;

import com.vdian.search.ann.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/16
 * Time: 12:22
 * 使用':'或者',' 进行分割
 */
public class VectorStringField extends VectorField {

	@Override
	public IndexableField createField(SchemaField field, Object value, float boost) {
		return super.createField(field, stringToBytes(value), boost);
	}

	@Override
	public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
		return super.createFields(field, stringToBytes(value), boost);
	}

	private byte[] stringToBytes(Object value){
		assert value instanceof String;
		String[] strVal = StringUtils.split((String)value,':');
		if(strVal.length <= 1){
			strVal = StringUtils.split((String)value,',');
		}

		float[] floatVal = new float[strVal.length];
		for(int i=0;i<strVal.length;i++){
			floatVal[i] = Float.parseFloat(strVal[i]);
		}
		byte[] byteVal = BytesUtils.floatsToBytes(floatVal);
		return byteVal;
	}
}
