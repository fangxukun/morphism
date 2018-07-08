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

/**
 * match is indexValue & queryValue == queryValue
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:44:51
 */
public class BitContainCollectorFilterable extends ValueSourceCollectorFilterable {

    protected final long queryBit;

    public BitContainCollectorFilterable(ValueSource valueSource, long queryBit) {
        super(valueSource);
        this.queryBit = queryBit;
    }

    @Override
    public String description() {
        return "cbit(" + valueSource.description() + "):" + queryBit;
    }

    @Override
    public boolean matches(int doc) {
        long indexObj = functionValues.longVal(doc);
        return (indexObj & queryBit) == queryBit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        BitContainCollectorFilterable that = (BitContainCollectorFilterable) o;

        if (queryBit != that.queryBit)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (queryBit ^ (queryBit >>> 32));
        return result;
    }
}
