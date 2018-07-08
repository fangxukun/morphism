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
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SyntaxError;

import java.util.List;

/**
 * fq={!cf name=bit}bit_field:(0b01100)
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:44:41
 */
public class BitCollectorFilterablePlugin extends CollectorFilterablePlugin {

    public static final String NAME_BIT = "bit";
    public static final String NAME_CONTAIN_BIT = "cbit";

    @Override
    public CollectorFilterable createCollectorFilterable(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req, ValueSource valueSource,
            String valueStr) throws SyntaxError {

        verifyValueStr(valueStr, valueSource);

        List<String> lvs = parseMultiValue(valueStr, null, true);
        long queryBit = 0;
        // queryBit = a | b | c ...
        for (String lv : lvs) {
            Long qv = null;
            try {
                qv = parseLongExt(lv);
            } catch (NumberFormatException e) {
                throw new SyntaxError(lv + " can't parse long for '" + getName() + "' cf", e);
            }
            if (qv != null) {
                queryBit |= qv.longValue();
            }
        }
        return createCollectorFilterable(valueSource, queryBit);
    }

    protected CollectorFilterable createCollectorFilterable(ValueSource valueSource, long queryBit) {
        return new BitCollectorFilterable(valueSource, queryBit);
    }

    @Override
    public String getName() {
        return NAME_BIT;
    }
}
