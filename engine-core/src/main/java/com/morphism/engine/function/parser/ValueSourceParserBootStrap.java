package com.morphism.engine.function.parser;

import com.morphism.engine.function.BytesFunction;
import com.morphism.engine.function.DiscountYuanFunction;
import com.morphism.engine.function.PriceFunction;
import com.morphism.engine.function.ShieldFunction;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

/**
 * User: xukun.fyp
 * Date: 16/12/1
 * Time: 18:45
 * 通用的ValueSource扩展
 *  solrConfig: <valueSourceParser name="customParsers" class="ValueSourceParserBootStrap" />
 */
public class ValueSourceParserBootStrap extends ValueSourceParser {
	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		return null;
	}

	static {
		addParser("price", new ValueSourceParser() {
			@Override
			public ValueSource parse(FunctionQParser fp) throws SyntaxError {
				ValueSource promotion  = fp.parseValueSource();
				ValueSource reservePrice = fp.parseValueSource();
				return new PriceFunction(promotion,reservePrice);
			}
		});

		addParser("discountyuan", new ValueSourceParser() {
			@Override
			public ValueSource parse(FunctionQParser fp) throws SyntaxError {
				ValueSource promotion = fp.parseValueSource();
				ValueSource reservePrice = fp.parseValueSource();
				return new DiscountYuanFunction(promotion,reservePrice);
			}
		});

		addParser("shield", new ValueSourceParser() {
			@Override
			public ValueSource parse(FunctionQParser fp) throws SyntaxError {
				ValueSource shield = fp.parseValueSource();
				int market = fp.parseInt();
				int order = fp.parseInt();
				return new ShieldFunction(shield,market,order);
			}
		});

		addParser("bytes", new ValueSourceParser() {
			@Override
			public ValueSource parse(FunctionQParser fp) throws SyntaxError {
				ValueSource bytes = fp.parseValueSource();
				int index = fp.parseInt();
				return new BytesFunction(bytes,index);
			}
		});

		addParser("geodistdv",new GeoDistDVParser());
	}

}
