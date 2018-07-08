package com.morphism.search.field.update;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.document.FieldType;

/**
 * User: xukun.fyp
 * Date: 17/4/13
 * Time: 11:40
 */
public class TextTransformers {

	public static final NumericTransformer numericTransformer(FieldType.NumericType type) throws UpdateException{
		switch (type){
			case INT:
				return new IntTransformer();
			case LONG:
				return new LongTransformer();
			case DOUBLE:
				return new DoubleTransformer();
			case FLOAT:
				return new FloatTransformer();
			default:
				throw new UpdateException("unsupported numeric type:" + type);
		}
	}

	public static final BinaryTransformer defaultBinaryTransformer(){
		return new Base64Transformer();
	}


	public interface KeyTransformer{
		byte[] transformer(String from);
	}

	public interface NumericTransformer{
		long transform(String from);
	}

	public interface BinaryTransformer{
		byte[] transform(String from);
	}

	public static class DoubleTransformer implements NumericTransformer{
		@Override
		public long transform(String from) {
			double doubleVal = Double.parseDouble(from);
			return Double.doubleToLongBits(doubleVal);
		}
	}

	public static class FloatTransformer implements NumericTransformer{

		@Override
		public long transform(String from) {
			float floatVal = Float.parseFloat(from);
			return Float.floatToIntBits(floatVal);
		}
	}

	public static class LongTransformer implements NumericTransformer{
		@Override
		public long transform(String from) {
			return Long.parseLong(from);
		}
	}

	public static class IntTransformer implements NumericTransformer{
		@Override
		public long transform(String from) {
			return Integer.parseInt(from);
		}
	}

	public static class Base64Transformer implements BinaryTransformer{

		@Override
		public byte[] transform(String base64) {
			return Base64.decodeBase64(base64);
		}
	}
}
