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
import org.apache.lucene.queries.function.ValueSourceScorer;
import org.apache.solr.search.function.ValueSourceRangeFilter;

import java.io.IOException;
import java.util.Map;

/**
 * range collector filter
 *
 * @see ValueSourceRangeFilter
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:10
 */
public class RangeCollectorFilterable extends CollectorFilterable {

    protected final ValueSourceRangeFilter valueSourceRangeFilter;

    protected ValueSourceScorer rangeScorer;

    public RangeCollectorFilterable(ValueSourceRangeFilter valueSourceRangeFilter) {
        this.valueSourceRangeFilter = valueSourceRangeFilter;
    }

    public ValueSource getValueSource() {
        return valueSourceRangeFilter.getValueSource();
    }

    public String getLowerVal() {
        return valueSourceRangeFilter.getLowerVal();
    }

    public String getUpperVal() {
        return valueSourceRangeFilter.getUpperVal();
    }

    public boolean isIncludeLower() {
        return valueSourceRangeFilter.isIncludeLower();
    }

    public boolean isIncludeUpper() {
        return valueSourceRangeFilter.isIncludeUpper();
    }

    @Override
    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("range(");
        sb.append(getValueSource().description());
        sb.append("):");
        sb.append(isIncludeLower() ? '[' : '(');
        sb.append(getLowerVal() == null ? "*" : getLowerVal());
        sb.append(" TO ");
        sb.append(getUpperVal() == null ? "*" : getUpperVal());
        sb.append(isIncludeUpper() ? ']' : ')');
        return sb.toString();
    }

    @Override
    public boolean matches(int doc) {
        if (rangeScorer != null) {
            return rangeScorer.matches(doc);
        }
        return true;
    }

    @Override
    public void doSetNextReader(@SuppressWarnings("rawtypes") Map context, LeafReaderContext readerContext) throws IOException {
        FunctionValues values = getValueSource().getValues(context, readerContext);
        rangeScorer = values.getRangeScorer(readerContext.reader(), getLowerVal(), getUpperVal(), isIncludeLower(), isIncludeUpper());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((valueSourceRangeFilter == null) ? 0 : valueSourceRangeFilter.hashCode());
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
        RangeCollectorFilterable other = (RangeCollectorFilterable) obj;
        if (valueSourceRangeFilter == null) {
            if (other.valueSourceRangeFilter != null)
                return false;
        } else if (!valueSourceRangeFilter.equals(other.valueSourceRangeFilter))
            return false;
        return true;
    }

}
