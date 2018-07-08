package com.vdian.engine.function;

import com.vdian.search.commons.antispam.ShieldV2;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
import org.apache.lucene.util.BytesRefBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 16/8/16
 * Time: 15:13
 * 屏蔽计算逻辑，详细逻辑见ShieldV2,通过传入的Market，Order，判断每个Doc是否需要屏蔽
 */
public class ShieldFunction extends ValueSource {
	private ValueSource 			shield;
	private int						market;
	private int						order;


	public ShieldFunction(ValueSource shield, int market, int order){
		this.shield = shield;
		this.market = market;
		this.order = order;
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		final FunctionValues shieldValues = shield.getValues(context,readerContext);

		final BytesRefBuilder builder = new BytesRefBuilder();
		final ShieldV2 shieldV2 = new ShieldV2();

		return new LongDocValues(this) {
			@Override
			public long longVal(int doc) {
				boolean exist = shieldValues.bytesVal(doc,builder);
				if(exist){
					shieldV2.fromValue(builder.bytes());
					boolean result = shieldV2.isShield(market,order);
					return result ? 1: 0;
				}else{
					return 0;
				}
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ShieldFunction))
			return false;

		ShieldFunction that = (ShieldFunction) o;

		if (market != that.market)
			return false;
		if (order != that.order)
			return false;
		return !(shield != null ? !shield.equals(that.shield) : that.shield != null);

	}

	@Override
	public int hashCode() {
		int result = shield != null ? shield.hashCode() : 0;
		result = 31 * result + market;
		result = 31 * result + order;
		return result;
	}

	@Override
	public String description() {
		return "shield(" + shield.description() + "," + market + "," + order + ")";

	}
}
