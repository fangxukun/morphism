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

import org.apache.lucene.queries.function.ValueSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * link sql <code>in=(x, y, z)</code>
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:45:54
 */
public class InCollectorFilterable extends ValueSourceCollectorFilterable {

    private final String[] inValues;

    private Set<Object> inObjValues;

    public InCollectorFilterable(ValueSource valueSource, String[] inValues) {
        super(valueSource);
        this.inValues = inValues;
    }

    @Override
    public String description() {
        return "in(" + valueSource.description() + "):" + Arrays.toString(inValues);
    }

    @Override
    public boolean matches(int doc) {
        Object obj = functionValues.objectVal(doc);
        if (obj == null) {
            return false;
        }
        if (inObjValues == null) {
            inObjValues = new HashSet<Object>(inValues.length * 2);
            for (Object objValue : parseValue(inValues, obj)) {
                inObjValues.add(objValue);
            }
        }
        return inObjValues.contains(obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(inValues);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InCollectorFilterable other = (InCollectorFilterable) obj;
        if (!Arrays.equals(inValues, other.inValues))
            return false;
        return true;
    }

}
