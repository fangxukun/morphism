package com.morphism.search.ann.lucene.function;

import com.morphism.search.ann.algorithm.FloatCalculator;
import com.morphism.search.ann.lucene.query.AnnQParserPlugin;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 16:44
 */
public class AnnValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		return null;
	}


	static{
		//TODO:API NOT COMPATIBLE
		addParser("annDistance", new ValueSourceParser() {
			@Override
			public ValueSource parse(FunctionQParser fp) throws SyntaxError {
				ValueSource annItem = fp.parseValueSource();
				float[] queryVector = FloatCalculator.strToFloatVector(fp.getParam(AnnQParserPlugin.VECTOR));
				return new AnnDistanceFunction(annItem,queryVector);
			}
		});
	}
}
