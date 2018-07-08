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

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.BitsFilteredDocIdSet;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.solr.search.SolrFilter;

import java.io.IOException;
import java.util.Map;

/**
 * CollectorFilter over a ValueSource
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:27
 */
public class ValueSourceCollectorFilter extends SolrFilter {

    private final ValueSource valueSource;
    private final CollectorFilterable collectorFilterable;

    public ValueSourceCollectorFilter(ValueSource valueSource, CollectorFilterable collectorFilterable) {
        this.valueSource = valueSource;
        this.collectorFilterable = collectorFilterable;
    }

    public ValueSourceCollectorFilter(ValueSourceCollectorFilterable valueSourceCollectorFilterable) {
        this(valueSourceCollectorFilterable.getValueSource(), valueSourceCollectorFilterable);
    }

    @Override
    public void createWeight(@SuppressWarnings("rawtypes") Map context, IndexSearcher searcher) throws IOException {
        valueSource.createWeight(context, searcher);
    }

    @Override
    public DocIdSet getDocIdSet(@SuppressWarnings("rawtypes")
    final Map context, final LeafReaderContext readerContext, Bits acceptDocs) throws IOException {
        collectorFilterable.doSetNextReader(context, readerContext);
        final LeafReader reader = readerContext.reader();
        final long maxDoc = reader.maxDoc();

        //TODO:老的API不支持，重新整理了下逻辑，需要测试...
        return new DocIdSet() {

            @Override
            public DocIdSetIterator iterator() throws IOException {
                return new DocIdSetIterator() {
                    private int     doc = -1;

                    @Override
                    public int docID() {
                        return doc;
                    }

                    @Override
                    public int nextDoc() throws IOException {
                        for (; ;) {
                            doc++;
                            if (doc >= maxDoc){
                                return doc = NO_MORE_DOCS;
                            }

                            if (collectorFilterable.matches(doc)){
                                return doc;
                            }
                        }
                    }

                    @Override
                    public int advance(int target) throws IOException {
                        doc = target - 1;
                        return nextDoc();
                    }

                    @Override
                    public long cost() {
                        return maxDoc;
                    }
                };
            }

            @Override
            public long ramBytesUsed() {
                return 0;
            }
        };
    }

    public String toString(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("vsfilter(");
        sb.append(valueSource).append(",");
        sb.append(collectorFilterable.description());
        sb.append(")");
        return sb.toString();
    }

    public ValueSource getValueSource() {
        return valueSource;
    }

    public CollectorFilterable getCollectorFilterable() {
        return collectorFilterable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collectorFilterable == null) ? 0 : collectorFilterable.hashCode());
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
        ValueSourceCollectorFilter other = (ValueSourceCollectorFilter) obj;
        if (collectorFilterable == null) {
            if (other.collectorFilterable != null)
                return false;
        } else if (!collectorFilterable.equals(other.collectorFilterable))
            return false;
        if (valueSource == null) {
            if (other.valueSource != null)
                return false;
        } else if (!valueSource.equals(other.valueSource))
            return false;
        return true;
    }

}
