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

/**
 * fq={!cf name=cbit}bit_field:(0b01100)
 * 
 * @author chenlinbin
 * @create 2015年10月8日 下午3:44:58
 */
public class BitContainCollectorFilterablePlugin extends BitCollectorFilterablePlugin {

    @Override
    protected CollectorFilterable createCollectorFilterable(ValueSource valueSource, long queryBit) {
        return new BitContainCollectorFilterable(valueSource, queryBit);
    }

    @Override
    public String getName() {
        return NAME_CONTAIN_BIT;
    }
}
