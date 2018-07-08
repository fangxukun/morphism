/*
 * Copyright 2011-2015. by Koudai Corporation.
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Koudai Corporation ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Koudai.
 */

package com.morphism.engine.function;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.apache.lucene.queries.function.valuesource.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author chenlinbin
 * @create 2015-12-30 11:11
 */
public class MapsFunction extends ValueSource {

    protected final ValueSource source;
    private final Map<Object, Double> mapValues;
    protected final String field;

    public MapsFunction(ValueSource source, Map<Object, Double> mapValues) {
        this.source = source;
        if (source instanceof FieldCacheSource) {
            this.field = ((FieldCacheSource) source).getField();
        } else {
            this.field = null;
        }
        if(mapValues == null) {
            mapValues = Collections.emptyMap();
        }
        this.mapValues = mapValues;
    }

    @Override
    public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
        final NumericDocValues arr = DocValues.getNumeric(readerContext.reader(), field);
        return new DoubleDocValues(this) {
            @Override
            public double doubleVal(int doc) {
                Double value = null;
                if (source instanceof IntFieldSource) {
                    value = mapValues.get((int) arr.get(doc));
                } else if (source instanceof LongFieldSource) {
                    value = mapValues.get(new Long(arr.get(doc)));
                } else if (source instanceof FloatFieldSource) {
                    value = mapValues.get(new Float(Float.intBitsToFloat((int) arr.get(doc))));
                } else if (source instanceof DoubleFieldSource) {
                    value = mapValues.get(new Double(Double.longBitsToDouble(arr.get(doc))));
                }

                if (value == null) {
                    return 0;
                } else {
                    return value;
                }
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MapsFunction that = (MapsFunction) o;

        if (source != null ? !source.equals(that.source) : that.source != null)
            return false;
        return mapValues != null ? mapValues.equals(that.mapValues) : that.mapValues == null;

    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (mapValues != null ? mapValues.hashCode() : 0);
        return result;
    }

    @Override
    public String description() {
        return "maps("+source.description()+", "+mapValues+")";
    }
}
