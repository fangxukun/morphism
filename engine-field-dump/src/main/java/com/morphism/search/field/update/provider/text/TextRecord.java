package com.morphism.search.field.update.provider.text;

import com.morphism.search.field.dump.DumpLayout;
import com.morphism.search.field.update.UpdateContext;
import com.morphism.search.field.update.provider.BinaryField;
import com.morphism.search.field.update.provider.Record;
import com.morphism.search.field.update.TextTransformers;
import com.morphism.search.field.update.UpdateException;
import com.morphism.search.field.update.provider.NumericField;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 11:11
 */
public class TextRecord  implements Record {
	private final UpdateContext context;

	private final NumericField[]				numericFields;
	private final BinaryField[]					binaryFields;
	private final String[]						numericFiledNames;
	private final String[]						binaryFieldNames;
	private final DumpLayout.FieldEntry			keyField;

	private final long[]						numericValues;
	private final BytesRef[]					binaryValues;

	private Term								keyTerm;

	public TextRecord(UpdateContext context) throws UpdateException {
		this.context = context;
		this.keyField = context.layout.keyField;

		List<DumpLayout.FieldEntry> numericList = new ArrayList<>();
		List<DumpLayout.FieldEntry> binaryList = new ArrayList<>();
		for(DumpLayout.FieldEntry updateField : context.layout.updateFields){
			DocValuesType docValuesType = context.docValueType(updateField.fieldName);
			if(docValuesType == DocValuesType.BINARY){
				binaryList.add(updateField);
				continue;
			}
			if(docValuesType == DocValuesType.NUMERIC){
				numericList.add(updateField);
			}
		}

		numericFields = new NumericField[numericList.size()];
		numericValues = new long[numericFields.length];
		numericFiledNames = new String[numericFields.length];
		for(int i=0;i<numericList.size();i++){
			int index = numericList.get(i).index;
			String fieldName = numericList.get(i).fieldName;
			TextTransformers.NumericTransformer transformer = context.numericTransformer(fieldName);

			numericFiledNames[i] = fieldName;
			numericFields[i] = new NumericField(index,fieldName,transformer);
		}

		binaryFields = new BinaryField[binaryList.size()];
		binaryValues = new BytesRef[binaryList.size()];
		binaryFieldNames = new String[binaryFields.length];
		for(int i=0;i<binaryList.size();i++){
			int index = binaryList.get(i).index;
			String fieldName = binaryList.get(i).fieldName;

			binaryFieldNames[i] = fieldName;
			binaryFields[i] = new BinaryField(index,fieldName, TextTransformers.defaultBinaryTransformer());
		}
	}

	public void reset(String record){
		String[] fields = StringUtils.splitPreserveAllTokens(record,context.layout.delimiter);

		for(int i=0;i<numericFields.length;i++){
			NumericField field = numericFields[i];
			String fieldVal = fields[field.index];
			numericValues[i] = field.transformer.transform(fieldVal);
		}

		for(int i=0;i<binaryFields.length;i++){
			BinaryField field = binaryFields[i];
			String fieldVal = fields[field.index];
			binaryValues[i] = new BytesRef(binaryFields[i].transformer.transform(fieldVal));
		}

		this.keyTerm = new Term(keyField.fieldName,context.indexedKey(fields[keyField.index]));
	}

	@Override
	public Term getKeyTerm() {
		return keyTerm;
	}

	@Override
	public String[] getNumericFields() {
		return numericFiledNames;
	}

	@Override
	public String[] getBinaryFields() {
		return binaryFieldNames;
	}


	@Override
	public long[] getNumericValues() {
		return numericValues;
	}

	@Override
	public BytesRef[] getBinaryValues() {
		return binaryValues;
	}



}
