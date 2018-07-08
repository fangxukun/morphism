package com.vdian.search.ann.lucene.function;

import com.vdian.search.ann.lucene.codecs.AnnBinaryDocValues;
import com.vdian.search.ann.lucene.codecs.AnnTreeReader;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.lucene.queries.function.valuesource.FieldCacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 16:36
 */
public class AnnDistanceFunction extends ValueSource {
	private static final Logger		LOGGER		= LoggerFactory.getLogger(AnnDistanceFunction.class);
	private FieldCacheSource 		annItemVS;
	private float[]					query;

	public AnnDistanceFunction(ValueSource annItemVS,float[] queryVector){
		this.annItemVS = (FieldCacheSource)annItemVS;
		this.query = queryVector;
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		final BinaryDocValues bdv = readerContext.reader().getBinaryDocValues(annItemVS.getField());

		if (!(bdv instanceof AnnBinaryDocValues)) {
			throw new IllegalStateException(String.format("field %s was not indexed with AnnBinaryDocValue,bdv:" + bdv.getClass().getName()));
		}
		LOGGER.warn("distance query:" + Arrays.toString(query));

		final AnnBinaryDocValues docValues = (AnnBinaryDocValues) bdv;
		final AnnTreeReader reader = docValues.getReader();

		final Measurer measurer = reader.createMeasurer(query);
		return new FloatDocValues(this) {
			@Override
			public float floatVal(int doc) {
				return measurer.distance(doc);
			}
		};
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AnnDistanceFunction))
			return false;

		AnnDistanceFunction that = (AnnDistanceFunction) o;

		if (annItemVS != null ? !annItemVS.equals(that.annItemVS) : that.annItemVS != null)
			return false;
		return Arrays.equals(query, that.query);

	}

	@Override
	public int hashCode() {
		int result = annItemVS != null ? annItemVS.hashCode() : 0;
		result = 31 * result + (query != null ? Arrays.hashCode(query) : 0);
		return result;
	}

	@Override
	public String description() {
		return "annDistance(" + annItemVS.description() + "," + query + ")";
	}
}
