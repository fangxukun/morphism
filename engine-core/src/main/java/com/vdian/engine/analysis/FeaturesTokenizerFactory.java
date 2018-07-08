package com.vdian.engine.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 16/3/15
 * Time: 17:38
 */
public class FeaturesTokenizerFactory extends TokenizerFactory {

	private final int	maxTokenLength;

	public FeaturesTokenizerFactory(Map<String, String> args) {
		super(args);
		this.maxTokenLength = getInt(args,"maxTokenLength", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
	}

	@Override
	public Tokenizer create(AttributeFactory factory) {
		return new FeaturesTokenizer(maxTokenLength);
	}
}
