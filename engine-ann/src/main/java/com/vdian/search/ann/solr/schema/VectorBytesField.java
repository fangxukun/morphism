package com.vdian.search.ann.solr.schema;

import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 11:29
 */
public class VectorBytesField extends VectorField {


	@Override
	public IndexableField createField(SchemaField field, Object value, float boost) {
		return super.createField(field, value, boost);
	}

	@Override
	public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
		return super.createFields(field, value, boost);
	}
}
