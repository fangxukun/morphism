package com.vdian.engine.function;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.apache.lucene.util.BytesRefBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 16/4/10
 * Time: 12:47
 * 解析Byte数组的DocValue，并返回指定下标的byte value
 */
public class BytesFunction extends ValueSource {
	private static final Logger		LOGGER	= LoggerFactory.getLogger(BytesFunction.class);
	private ValueSource 			bytesValueSource;
	private int						index;

	public BytesFunction(ValueSource bytesValueSource, int indexSource) {
		this.bytesValueSource = bytesValueSource;
		this.index = indexSource;
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		final FunctionValues bytesValue = bytesValueSource.getValues(context,readerContext);
		final BytesRefBuilder builder = new BytesRefBuilder();

		return new IntDocValues(this) {
			@Override
			public int intVal(int doc) {
				boolean exist = bytesValue.bytesVal(doc,builder);
				byte[] bytes = builder.bytes();
				if(exist && index < builder.length()){
					return bytes[index];
				}else{
					return 0;
				}
			}
		};
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof BytesFunction))
			return false;

		BytesFunction that = (BytesFunction) o;

		if (index != that.index)
			return false;
		return !(bytesValueSource != null ? !bytesValueSource.equals(that.bytesValueSource) : that.bytesValueSource != null);

	}

	@Override
	public int hashCode() {
		int result = bytesValueSource != null ? bytesValueSource.hashCode() : 0;
		result = 31 * result + index;
		return result;
	}

	@Override
	public String description() {
		return "bytes(" + bytesValueSource.description() + "," + index +")" ;
	}
}
