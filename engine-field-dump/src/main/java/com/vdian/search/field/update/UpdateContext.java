package com.vdian.search.field.update;

import com.vdian.search.field.dump.DumpLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 17/4/13
 * Time: 11:35
 */
public class UpdateContext {
	public final DumpLayout					layout;
	public final IndexSchema				schema;
	public final Map<String,FieldInfo>		fis;
	public final BytesRefBuilder			refBuilder;


	public UpdateContext(DumpLayout layout,IndexSchema schema,Map<String,FieldInfo> fis){
		this.layout = layout;
		this.schema = schema;
		this.fis = fis;
		this.refBuilder = new BytesRefBuilder();
	}

	public void checkFields() throws UpdateException {
		if(!StringUtils.equals(this.schema.getUniqueKeyField().getName(),layout.keyField.fieldName)){
			throw new UpdateException(String.format("key field in layout not match with engine,layoutKeyField:%s,engineKeyField:%s",layout.keyField.fieldName));
		}

		for(DumpLayout.FieldEntry field : layout.updateFields){
			SchemaField schemaField = this.schema.getField(field.fieldName);
			if(!schemaField.hasDocValues()){
				throw new UpdateException(String.format("field %s do not has doc value,failed to update!",field.fieldName));
			}

			if(schemaField.indexed()){
				throw new UpdateException(String.format("field %s is indexed,can not update!",field.fieldName));
			}

			if(schemaField.stored()){
				throw new UpdateException(String.format("filed %s is stored,can not update! > 5.5 can retrieve doc from doc value",field.fieldName));
			}

			FieldInfo fi = fis.get(field.fieldName);
			if(fi.getDocValuesType() != DocValuesType.BINARY  && fi.getDocValuesType() != DocValuesType.NUMERIC){
				throw new UpdateException(String.format("field update only support numeric&binary,engine docValueType:" + fi.getDocValuesType()));
			}
		}
	}

	public DocValuesType docValueType(String fieldName){
		return fis.get(fieldName).getDocValuesType();
	}

	public TextTransformers.NumericTransformer numericTransformer(String fieldName) throws UpdateException {
		FieldType.NumericType numericType = schema.getField(fieldName).getType().getNumericType();
		return TextTransformers.numericTransformer(numericType);
	}

	public BytesRef indexedKey(String keyStrVal){
		refBuilder.clear();
		schema.getUniqueKeyField().getType().readableToIndexed(keyStrVal,refBuilder);
		return refBuilder.get();
	}
}
