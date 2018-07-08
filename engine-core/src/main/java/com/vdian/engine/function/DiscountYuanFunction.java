package com.vdian.engine.function;

import com.koudai.rio.commons.io.impl.NeuronByteArrayReader;
import com.vdian.search.commons.promotion.PromotionField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.apache.lucene.util.BytesRefBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xukun.fyp
 * Date: 16/6/7
 * Time: 18:11
 * 优惠价计算，店铺内搜索使用的是double标示价格。故...
 */
public class DiscountYuanFunction extends ValueSource {
	private static final Logger		LOGGER		= LoggerFactory.getLogger(DiscountYuanFunction.class);
	private ValueSource 			promotion;
	private ValueSource 			reservePrice;

	public DiscountYuanFunction(ValueSource promotion, ValueSource reservePrice){
		this.promotion = promotion;
		this.reservePrice = reservePrice;
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		final FunctionValues promotionValues = promotion.getValues(context, readerContext);
		final FunctionValues priceValues = reservePrice.getValues(context,readerContext);

		final BytesRefBuilder builder = new BytesRefBuilder();
		final PromotionField field = new PromotionField();
		final NeuronByteArrayReader reader = new NeuronByteArrayReader();

		final AtomicInteger failed = new AtomicInteger();

		return new DoubleDocValues(this) {
			@Override
			public double doubleVal(int doc) {
				boolean exist = promotionValues.bytesVal(doc,builder);
				double reservePrice = priceValues.doubleVal(doc);
				if(exist){
					try{
						reader.init(builder.bytes(),0,builder.length());
						field.readFields(reader);
						long discount = field.discountPrice((long)(reservePrice * 100));
						return (double)discount/100.0d;
					}catch (Exception e){
						failed.incrementAndGet();
						if(failed.get() % 1000 == 0){
							LOGGER.error(String.format("Parse failed,exist:%s,reservePrice:%s,failed:%s,doc:%s,bytes:%s",
									exist,reservePrice,failed.get(),doc, Arrays.toString(builder.bytes())),e);
						}
						return reservePrice;
					}

				}else{
					return reservePrice;
				}
			}
		};
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DiscountYuanFunction))
			return false;

		DiscountYuanFunction that = (DiscountYuanFunction) o;

		if (promotion != null ? !promotion.equals(that.promotion) : that.promotion != null)
			return false;
		return !(reservePrice != null ? !reservePrice.equals(that.reservePrice) : that.reservePrice != null);

	}

	@Override
	public int hashCode() {
		int result = promotion != null ? promotion.hashCode() : 0;
		result = 31 * result + (reservePrice != null ? reservePrice.hashCode() : 0);
		return result;
	}

	@Override
	public String description() {
		return "discountYuan(" + promotion.description() + "," + reservePrice.description() + ")";
	}
}
