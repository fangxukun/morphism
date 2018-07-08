package com.vdian.search.ann.solr.schema;

import com.vdian.search.ann.AnnLayout;
import com.vdian.search.ann.BytesUtils;
import com.vdian.search.ann.DataType;
import com.vdian.search.ann.lucene.AnnLayouts;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.BytesRefFieldSource;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.util.Base64;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.uninverting.UninvertingReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: xukun.fyp
 * Date: 15/12/15
 * Time: 10:36
 *
 * usage schema.xml
 *
 *	<fieldType name="vector" class="com.vdian.search.ann.solr.schema.VectorBytesField"
 *		docValues="true"
 *		docValuesFormat="ann"
 *		numOfTree="80"
 *		dataType="BYTE"
 *		leafNodeMaxItem="80",
 *		diskIndex="false"			//current not support true
 *		/>
 *
 */
public class VectorField extends FieldType {
	private AnnLayout			annLayout;


	@Override
	protected void init(IndexSchema schema, Map<String, String> args) {
		super.init(schema, args);

		AnnLayout defaultAnn = AnnLayout.annLayout();

		int numOfTree = getInt(args, "numOfTree", defaultAnn.numOfTree);
		DataType dataType = DataType.valueOf(getString(args, "dataType", defaultAnn.dataType.name()));
		int leafNodeMaxItem = getInt(args, "leafNodeMaxItem", defaultAnn.leafNodeMaxItem);
		boolean diskIndex = getBoolean(args,"diskIndex",defaultAnn.diskIndex);

		annLayout = new AnnLayout(numOfTree,defaultAnn.meanIterationStep,leafNodeMaxItem,dataType,diskIndex);
	}

	@Override
	protected void setArgs(IndexSchema schema, Map<String, String> args) {
		try{
			super.setArgs(schema, args);
		}catch (RuntimeException e){
			// ignore invalid arguments check!
		}
	}

	private String getString(Map<String,String> map,String key,String defaultVal){
		if(map.containsKey(key)){
			return map.get(key);
		}else{
			return defaultVal;
		}
	}

	private int getInt(Map<String,String> map,String key,int defaultVal){
		if(map.containsKey(key)){
			return Integer.parseInt(map.get(key));
		}else{
			return defaultVal;
		}
	}
	private boolean getBoolean(Map<String,String> map,String key,boolean defaultVal){
		if(map.containsKey(key)){
			return Boolean.parseBoolean(map.get(key));
		}else{
			return defaultVal;
		}
	}

	@Override
	public UninvertingReader.Type getUninversionType(SchemaField sf) {
		if(sf.multiValued()){
			throw new RuntimeException("Unsupported multiValue in VectorField");
		}else{
			return UninvertingReader.Type.BINARY;
		}
	}

	@Override
	public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
		final float[] floatsVal = BytesUtils.floatsFromBytes(f.binaryValue().bytes);
		writer.writeArray(name, new Iterator() {
			int i=0;
			@Override
			public boolean hasNext() {
				return i < floatsVal.length;
			}

			@Override
			public Object next() {
				return floatsVal[i++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

	@Override
	public SortField getSortField(SchemaField field, boolean top) {
		return null;
	}

	@Override
	public String toExternal(IndexableField f) {
		final float[] floatsVal = BytesUtils.floatsFromBytes(f.binaryValue().bytes);
		return Arrays.toString(floatsVal);
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

		if(AnnLayouts.notExist(field.getName())){
			AnnLayouts.register(field.getName(),annLayout);
		}
		return new BinaryDocValuesField(field.getName(),bytesRef);
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

		if(AnnLayouts.notExist(field.getName())){
			AnnLayouts.register(field.getName(),annLayout);
		}

		if(field.stored()){
			StoredField storedField = new StoredField(field.getName(),bytesRef);
			fields.add(storedField);
		}
		IndexableField indexableField = new BinaryDocValuesField(field.getName(),bytesRef);
		fields.add(indexableField);
		return fields;
	}
}
