package com.vdian.engine.schema;

import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.BytesRefFieldSource;
import org.apache.lucene.search.SortField;
import org.apache.lucene.uninverting.UninvertingReader;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.util.Base64;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 16/8/12
 * Time: 10:34
 * 如果很多byt[]都是相同的，所以使用SortedDocValuesField。
 * 比如屏蔽降权的大部分逻辑都应该是一致的
 */
public class BinarySortedDVField extends FieldType {

	@Override
	public UninvertingReader.Type getUninversionType(SchemaField sf) {
		if(sf.multiValued()){
			throw new RuntimeException("Unsupported multiValue in BinaryDVField");
		}else{
			return UninvertingReader.Type.BINARY;
		}
	}

	@Override
	public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
		BytesRef br = f.binaryValue();
		writer.writeByteArr(name,br.bytes,br.offset,br.length);
	}

	@Override
	public SortField getSortField(SchemaField field, boolean top) {
		return null;
	}

	@Override
	public String toExternal(IndexableField f) {
		ByteBuffer buf = toObject(f);
		return Base64.byteArrayToBase64(buf.array(), buf.position(), buf.limit() - buf.position());
	}

	@Override
	public ByteBuffer toObject(IndexableField f) {
		BytesRef bytes = f.binaryValue();
		return  ByteBuffer.wrap(bytes.bytes, bytes.offset, bytes.length);
	}


	@Override
	public void checkSchemaField(SchemaField field) {

	}

	@Override
	public ValueSource getValueSource(SchemaField field, QParser parser) {
		field.checkFieldCacheSource();
		return new BytesRefFieldSource(field.getName());
	}

	@Override
	public IndexableField createField(SchemaField field, Object value, float boost) {
		if(value == null){
			return null;
		}

		BytesRef bytesRef = null;
		if(value instanceof byte[]){
			byte[] buf = (byte[]) value;
			bytesRef = new BytesRef(buf,0,buf.length);
		}else if(value instanceof BytesRef){
			bytesRef = (BytesRef)value;
		}else if (value instanceof ByteBuffer && ((ByteBuffer)value).hasArray()){
			ByteBuffer buf = (ByteBuffer)value;
			bytesRef = new BytesRef(buf.array(),buf.position(),buf.limit());
		}else{
			byte[] buf = Base64.base64ToByteArray(value.toString());
			bytesRef = new BytesRef(buf,0,buf.length);
		}
		return new SortedDocValuesField(field.getName(),bytesRef);
	}

	@Override
	public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
		List<IndexableField> fields = new ArrayList<>(2);

		if(value == null){
			return null;
		}

		BytesRef bytesRef = null;
		if(value instanceof byte[]){
			byte[] buf = (byte[]) value;
			bytesRef = new BytesRef(buf,0,buf.length);
		}else if(value instanceof BytesRef){
			bytesRef = (BytesRef)value;
		}else if (value instanceof ByteBuffer && ((ByteBuffer)value).hasArray()){
			ByteBuffer buf = (ByteBuffer)value;
			bytesRef = new BytesRef(buf.array(),buf.position(),buf.limit());
		}else{
			byte[] buf = Base64.base64ToByteArray(value.toString());
			bytesRef = new BytesRef(buf,0,buf.length);
		}

		if(field.stored()){
			StoredField storedField = new StoredField(field.getName(),bytesRef);
			fields.add(storedField);
		}
		IndexableField indexableField = new SortedDocValuesField(field.getName(),bytesRef);
		fields.add(indexableField);
		return fields;
	}
}
