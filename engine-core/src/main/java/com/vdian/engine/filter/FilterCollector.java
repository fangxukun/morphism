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
import org.apache.solr.search.DelegatingCollector;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:45:50
 */
public class FilterCollector extends DelegatingCollector {

    protected final CollectorFilterable filter;
    @SuppressWarnings("rawtypes")
    protected final Map fcontext;
    int maxdoc;

    public FilterCollector(CollectorFilterable filterableCollector, @SuppressWarnings("rawtypes") Map fcontext) {
        this.filter = filterableCollector;
        this.fcontext = fcontext;
    }

    @Override
    public void collect(int doc) throws IOException {
        if (doc < maxdoc && filter.matches(doc)) {
            leafDelegate.collect(doc);
        }
    }

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        maxdoc = context.reader().maxDoc();
        filter.doSetNextReader(fcontext, context);
        super.doSetNextReader(context);
    }

}