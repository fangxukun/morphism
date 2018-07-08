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

package com.vdian.engine.filter;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:34
 */
public abstract class ValueSourceCollectorFilterable extends CollectorFilterable {

    protected ValueSource valueSource;

    protected FunctionValues functionValues;


    public ValueSourceCollectorFilterable(ValueSource valueSource) {
        this.valueSource = valueSource;
    }

    public ValueSource getValueSource() {
        return valueSource;
    }

    @Override
    public void doSetNextReader(@SuppressWarnings("rawtypes") Map context, LeafReaderContext readerContext) throws IOException {
        functionValues = valueSource.getValues(context, readerContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((valueSource == null) ? 0 : valueSource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueSourceCollectorFilterable other = (ValueSourceCollectorFilterable) obj;
        if (valueSource == null) {
            if (other.valueSource != null)
                return false;
        } else if (!valueSource.equals(other.valueSource))
            return false;
        return true;
    }

}
