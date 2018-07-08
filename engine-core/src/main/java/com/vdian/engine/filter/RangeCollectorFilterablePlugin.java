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
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.function.ValueSourceRangeFilter;

/**
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:21
 */
public class RangeCollectorFilterablePlugin extends CollectorFilterablePlugin {

    public static final String NAME = "range";

    @Override
    public CollectorFilterable createCollectorFilterable(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req, ValueSource valueSource,
            String valueStr) throws SyntaxError {

        ValueSourceRangeFilter rf = null;
        if (valueStr == null) {
            String l = localParams.get("l");
            String u = localParams.get("u");
            boolean includeLower = localParams.getBool("incl", true);
            boolean includeUpper = localParams.getBool("incu", true);
            rf = new ValueSourceRangeFilter(valueSource, l, u, includeLower, includeUpper);
        } else {
            verifyValueStr(valueStr, valueSource);
            rf = parseRange(valueSource, valueStr);
        }

        return new RangeCollectorFilterable(rf);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
