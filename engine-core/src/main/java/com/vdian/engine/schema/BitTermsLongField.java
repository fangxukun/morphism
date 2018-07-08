package com.vdian.engine.schema;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieLongField;
import org.apache.solr.search.QParser;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 16/3/15
 * Time: 15:22
 * 将Long值进行索引的同时，抽出每个Bit的作为Term的作为索引字段存储
 * value:7将被拆分为1,2,4,7 这四个term存储。
 */
public class BitTermsLongField extends TrieLongField {

	@Override
	public List<IndexableField> createFields(SchemaField sf, Object value, float boost) {
		List<IndexableField> fields = new ArrayList<>();
		fields.addAll(super.createFields(sf, value, boost));

		Long longVal = Long.valueOf(value.toString());
		List<Long> bitValues =  bitsArray(longVal);
		for(Long bitValue : bitValues){
			fields.add(createTermOnlyField(sf, bitValue, boost));
		}
		return fields;
	}

	private IndexableField createTermOnlyField(SchemaField field, Object value, float boost) {
		FieldType ft = new FieldType();
		ft.setStored(false);
		ft.setTokenized(false);
		ft.setOmitNorms(field.omitNorms());
		ft.setIndexOptions(getIndexOptions(field, value.toString()));

		ft.setNumericType(FieldType.NumericType.LONG);
		ft.setNumericPrecisionStep(precisionStep);

		final org.apache.lucene.document.Field f;

		long l = (value instanceof Number)
				? ((Number)value).longValue()
				: Long.parseLong(value.toString());
		f = new org.apache.lucene.document.LongField(field.getName(), l, ft);

		f.setBoost(boost);
		return f;
	}


	/**
	 * 查询时，通常只会查询Bit位的Val(1,2,4,8...),这种没有问题，不过有可能会查询6这种非bit位Value
	 * 此时将用BooleanQuery来拼接查询。
	 *
	 * @param parser
	 * @param field
	 * @param externalVal
	 * @return
	 */
	@Override
	public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
		Long longVal = Long.parseLong(externalVal);
		List<Long> bitValues =  bitsArray(longVal);
		if(bitValues.size() > 1){
			BooleanQuery.Builder builder = new BooleanQuery.Builder();

			for(Long bitVal : bitValues){
				BytesRefBuilder br = new BytesRefBuilder();
				readableToIndexed(String.valueOf(bitVal), br);
				builder.add(new TermQuery(new Term(field.getName(), br.toBytesRef())), BooleanClause.Occur.MUST);
			}

			return new ConstantScoreQuery(builder.build());
		}else{
			return super.getFieldQuery(parser,field,externalVal);
		}
	}

	public static List<Long> bitsArray(Long value){
		List<Long> values = new ArrayList<>();
		for(int i=0;i<64;i++){
			long bitVal = 1L<<i;
			if((bitVal & value) > 0 && bitVal <= value){
				values.add(bitVal);
			}
		}
		return values;
	}
}
