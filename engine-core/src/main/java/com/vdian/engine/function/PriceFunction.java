package com.vdian.engine.function;

import com.koudai.rio.commons.io.impl.NeuronByteArrayReader;
import com.vdian.search.commons.promotion.PromotionField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
import org.apache.lucene.util.BytesRefBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xukun.fyp
 * Date: 15/12/11
 * Time: 15:35
 * 优惠价格计算逻辑，通过优惠规则和原始价格结合计算最终价格。优惠规则目前采用Bytes存储
 */
public class PriceFunction extends ValueSource {
	private static final Logger		LOGGER		= LoggerFactory.getLogger(PriceFunction.class);
	private ValueSource 			promotion;
	private ValueSource 			reservePrice;

	public PriceFunction(ValueSource promotion, ValueSource reservePrice){
		this.promotion = promotion;
		this.reservePrice = reservePrice;
	}

	@Override
	public FunctionValues getValues(Map context, final LeafReaderContext readerContext) throws IOException {
		final FunctionValues promotionValues = promotion.getValues(context, readerContext);
		final FunctionValues priceValues = reservePrice.getValues(context,readerContext);

		final BytesRefBuilder builder = new BytesRefBuilder();
		final PromotionField field = new PromotionField();
		final NeuronByteArrayReader reader = new NeuronByteArrayReader();

		final AtomicInteger failed = new AtomicInteger();
		return new LongDocValues(this) {
			@Override
			public long longVal(int doc) {

				boolean exist = promotionValues.bytesVal(doc,builder);
				long reservePrice = priceValues.longVal(doc);
				reader.init(builder.bytes(),0,builder.length());
				if(exist){
					try{
						field.readFields(reader);
						return field.discountPrice(reservePrice);
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
		if (!(o instanceof PriceFunction))
			return false;

		PriceFunction that = (PriceFunction) o;

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
		return "price(" + promotion.description() + "," + reservePrice.description() + ")";
	}
}
