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

import java.util.List;

/**
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:46:06
 */
public class InCollectorFilterablePlugin extends CollectorFilterablePlugin {

    public static final String NAME = "in";

    @Override
    public CollectorFilterable createCollectorFilterable(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req, ValueSource valueSource,
            String valueStr) throws SyntaxError {

        verifyValueStr(valueStr, valueSource);

        List<String> lvs = parseMultiValue(valueStr, null, true);
        String[] inValues = lvs.toArray(new String[lvs.size()]);
        return new InCollectorFilterable(valueSource, inValues);
    }

    @Override
    public String getName() {
        return NAME;
    }

}