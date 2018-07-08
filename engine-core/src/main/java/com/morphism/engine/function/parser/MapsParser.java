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

package com.morphism.engine.function.parser;

import com.google.common.base.Splitter;
import com.morphism.engine.function.MapsFunction;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.*;
import org.apache.solr.common.StringUtils;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenlinbin
 * @create 2015-12-30 11:26
 */
public class MapsParser extends ValueSourceParser {

    private interface ValueParser<T> {

        T parse(String value);
    }

    private ValueParser vp;

    private class LongValueParser implements ValueParser<Long> {
        @Override
        public Long parse(String value) {
            return Long.parseLong(value);
        }
    }

    private class IntValueParser implements ValueParser<Integer> {
        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }
    }

    private class DoubleValueParser implements ValueParser<Double> {
        @Override
        public Double parse(String value) {
            return Double.parseDouble(value);
        }
    }

    private class FloatValueParser implements ValueParser<Float> {
        @Override
        public Float parse(String value) {
            return Float.parseFloat(value);
        }
    }

    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
        ValueSource vs = fp.parseValueSource();
        if(vs instanceof FieldCacheSource) {
            if (vs instanceof IntFieldSource) {
                vp = new IntValueParser();
            } else if (vs instanceof LongFieldSource) {
                vp = new LongValueParser();
            } else if (vs instanceof DoubleFieldSource) {
                vp = new DoubleValueParser();
            } else if (vs instanceof FloatFieldSource) {
                vp = new FloatValueParser();
            }
        }
        String mv = fp.parseArg();
        if(StringUtils.isEmpty(mv)) {
            throw new SyntaxError("maps(vs, str) function miss 'str'");
        }
        return new MapsFunction(vs, parseMapValues(mv));
    }

    private Map<Object, Double> parseMapValues(String mapValues) throws SyntaxError {
        Map<Object, Double> mv = new HashMap<>();
        for(String v : Splitter.on(';').split(mapValues)) {
            String[] av = v.split(":");
            if(av == null || av.length < 2) {
                throw new SyntaxError("maps(vs, str) function, 'str' format error, required 'k:double;k2:double'");
            }
            Object vkey = vp.parse(av[0]);
            Double vv = Double.parseDouble(av[1]);
            mv.put(vkey, vv);
        }
        return mv;
    }
}
