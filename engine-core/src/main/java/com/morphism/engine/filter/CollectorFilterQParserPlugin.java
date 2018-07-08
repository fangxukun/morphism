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

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import java.util.Map;

/**
 * not use query cache
 *
 * example:<p/>
 * <code>fq={!cf name=in}status:(-1, 2)</code><br/>
 * <code>fq={!cf name=in not=true}status:(3,4)</code><br/>
 * <code>fq={!cf name=range}price:[100 TO 500]</code><br/>
 * <code>fq={!cf name=range}log(page_view):[50 TO 120]</code>
 *
 *
 * solrConfig: <queryParser name="cf" class="CollectorFilterQParserPlugin"/>
 * @author chenlinbin
 * @create 2015年10月8日 下午3:45:35
 */
public class CollectorFilterQParserPlugin extends QParserPlugin {

    public static final String NAME = "cf";

    protected Map<String, CollectorFilterablePlugin> customPlugins = null;

    @SuppressWarnings("rawtypes")
    @Override
    public void init(NamedList args) {
        // TODO init custom CollectorFilterablePlugins
    }

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new CollectorFilterQParser(qstr, localParams, params, req, customPlugins);
    }

}
