package com.vdian.engine.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.solr.analysis.SolrAnalyzer;

import java.io.IOException;


/**
 * CopyFrom org.apache.solr.schema.FieldType.DefaultAnalyzer
 */
public class DefaultAnalyzer extends SolrAnalyzer {
	final int maxChars;

	public DefaultAnalyzer(int maxChars) {
		this.maxChars=maxChars;
	}

	public DefaultAnalyzer(){
		this(255);
	}

	@Override
	public TokenStreamComponents createComponents(String fieldName) {
		Tokenizer ts = new Tokenizer() {
			final char[] cbuf = new char[maxChars];
			final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
			final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
			@Override
			public boolean incrementToken() throws IOException {
				clearAttributes();
				int n = input.read(cbuf,0,maxChars);
				if (n<=0) return false;
				String s = new String(cbuf,0,n);
				termAtt.setEmpty().append(s);
				offsetAtt.setOffset(correctOffset(0),correctOffset(n));
				return true;
			}
		};

		return new TokenStreamComponents(ts);
	}
}