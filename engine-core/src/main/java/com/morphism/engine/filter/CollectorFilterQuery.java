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

package com.morphism.engine.filter;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.PostFilter;
import org.apache.solr.search.SolrConstantScoreQuery;

import java.util.Map;

/**
 * use {@link PostFilter} and {@link FilterCollector} filter docId
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:45:43
 */
public class CollectorFilterQuery extends SolrConstantScoreQuery implements PostFilter {

    final ValueSourceCollectorFilter valueSourceFilter;

    public CollectorFilterQuery(ValueSourceCollectorFilter valueSourceFilter) {
        super(valueSourceFilter);
        super.setCache(false);
        super.setCost(120);
        this.valueSourceFilter = valueSourceFilter;
    }

    @Override
    public DelegatingCollector getFilterCollector(IndexSearcher searcher) {
        @SuppressWarnings("rawtypes")
        Map fcontext = ValueSource.newContext(searcher);
        return new FilterCollector(valueSourceFilter.getCollectorFilterable(), fcontext);
    }

    @Override
    public void setCache(boolean cache) {
        if (cache) {
            throw new SolrException(ErrorCode.BAD_REQUEST, CollectorFilterQuery.class.getSimpleName() + " not support 'cache=true'");
        }
        super.setCache(cache);
    }
}
