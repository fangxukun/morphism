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

import java.io.IOException;
import java.util.Map;

/**
 * wrapped <code>NOT</code> {@link CollectorFilterable}
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:44
 */
public class WrappedNotCollectorFilterable extends CollectorFilterable {

    private final CollectorFilterable collectorFilterable;

    public WrappedNotCollectorFilterable(CollectorFilterable collectorFilterable) {
        this.collectorFilterable = collectorFilterable;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WrappedNotCollectorFilterable) {
            return this.collectorFilterable.equals(((WrappedNotCollectorFilterable) o).collectorFilterable);
        }
        return collectorFilterable.equals(o);
    }

    @Override
    public int hashCode() {
        return collectorFilterable.hashCode();
    }

    @Override
    public String description() {
        return "not." + collectorFilterable.description();
    }

    @Override
    public boolean matches(int doc) {
        return !collectorFilterable.matches(doc);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void doSetNextReader(Map context, LeafReaderContext readerContext) throws IOException {
        collectorFilterable.doSetNextReader(context, readerContext);
    }

}
