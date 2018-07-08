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

import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;

import java.util.Map;

/**
 * cf QParser
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:45:27
 */
public class CollectorFilterQParser extends FunctionQParser {

    public static final String CF_NAME = "name";
    public static final String CF_NOT = "not";

    protected Map<String, CollectorFilterablePlugin> customPlugins = null;

    public CollectorFilterQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req,
            Map<String, CollectorFilterablePlugin> customPlugins) {
        super(qstr, localParams, params, req);
        setParseMultipleSources(false);
        setParseToEnd(false);
        this.customPlugins = customPlugins;
    }

    public CollectorFilterablePlugin getCollectorFilterablePlugin(String name) {
        CollectorFilterablePlugin cfPlugin = CollectorFilterablePlugin.standardPlugins.get(name);
        if (cfPlugin == null && customPlugins != null) {
            cfPlugin = customPlugins.get(name);
        }
        return cfPlugin;
    }

    @Override
    public Query parse() throws SyntaxError {
        if (localParams == null) {
            throw new SolrException(ErrorCode.BAD_REQUEST, CollectorFilterQParserPlugin.NAME + " QParser miss localParams");
        }
        String name = localParams.get(CF_NAME);
        CollectorFilterablePlugin cfPlugin = getCollectorFilterablePlugin(name);
        if (cfPlugin == null) {
            throw new SolrException(ErrorCode.BAD_REQUEST, CollectorFilterQParserPlugin.NAME + " QParser not found '" + name + "' CollectorFilterablePlugin");
        }

        // parse func for vs
        FunctionQuery funQ = (FunctionQuery) super.parse();
        ValueSource vs = funQ.getValueSource();

        // try parse value
        String valueStr = null;
        int valueIndex = qstr.indexOf(":");
        if (valueIndex > 0 && valueIndex < qstr.length() - 1) {
            valueStr = qstr.substring(valueIndex + 1);
        }

        // create cf
        CollectorFilterable cf = cfPlugin.createCollectorFilterable(name, localParams, localParams, req, vs, valueStr);

        if (localParams.getBool(CF_NOT, false)) {
            // negative cf
            cf = new WrappedNotCollectorFilterable(cf);
        }

        return new CollectorFilterQuery(new ValueSourceCollectorFilter(vs, cf));
    }

}
