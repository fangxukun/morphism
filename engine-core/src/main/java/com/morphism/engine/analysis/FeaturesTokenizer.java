package com.morphism.engine.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * User: xukun.fyp
 * Date: 16/3/4
 * Time: 15:13
 *
 * 对于Feature字段做分词，支持如下格式的features:
 * p1:v11,v12|desc11,desc12;p2:v2;p2:
 */
public class FeaturesTokenizer extends Tokenizer {

	public static final int				TERM_MAX_SIZE		=	1024;

	private final CharTermAttribute 	termAtt;

	//进入索引的白名单,可以忽略掉，不用
	private Set<String>					allowKeys;

	private int 						maxTokenLength;

	//形如:p1:v1,v2,v3|desc1,desc2,desc3;p2:v21,v22;p3:v33;p4:v44
	private final char					kvSegmentsDelimiter		= ';';			//;
	private final char					kvDelimiter				= ':';			//:
	private final char					valueSegmentsDelimiter	= '|';			//|
	private final char					valuesDelimiter			= ',';			//,

	public FeaturesTokenizer(int maxTokenLength){
		termAtt = addAttribute(CharTermAttribute.class);
		termAtt.resizeBuffer(TERM_MAX_SIZE);

		this.maxTokenLength = maxTokenLength;
		this.allowKeys = new HashSet<>();
	}

	public FeaturesTokenizer(){
		this(256);
	}


	boolean inSegment = false;
	int keyLength = 0;
	final CharBuffer charBuffer = CharBuffer.allocate(TERM_MAX_SIZE);

	@Override
	public boolean incrementToken() throws IOException {
		int c = -1;
		clearAttributes();
		while((c = input.read())!= -1){
			charBuffer.append((char)c);
			int termLen = 0;

			switch (c){
				case kvDelimiter:				// :
					inSegment = true;
					keyLength = charBuffer.position() -1;
					termLen = writeTermAtt();
					charBuffer.mark();
					break;
				case valuesDelimiter:			// ,
					termLen = writeTermAtt();
					charBuffer.reset();
					break;
				case valueSegmentsDelimiter:	// |
					termLen = writeTermAtt();
					charBuffer.reset();
					inSegment = false;				//对于描述不索引
					break;
				case kvSegmentsDelimiter:		// ;
					termLen = writeTermAtt();
					charBuffer.clear();
					inSegment = false;
					break;
				default:
			}

			if(termLen > 0){
				return true;
			}
		}

		// 处理最后留下的那部分
		if(checkTermAttr()){
			termAtt.copyBuffer(charBuffer.array(),0,charBuffer.position());
			charBuffer.clear();
			return true;
		}


		return false;
	}

	private int writeTermAtt(){
		if(checkTermAttr()){
			termAtt.copyBuffer(charBuffer.array(), 0, charBuffer.position() - 1);
			return charBuffer.position() - 1;
		}
		return 0;
	}

	private boolean checkTermAttr(){
		if(!(charBuffer.position() > 0 && inSegment)){
			return false;
		}

		if(allowKeys.size() > 0){
			String key = new String(charBuffer.array(),0,keyLength);
			return allowKeys.contains(key);
		}

		return true;
	}

	public static void main(String[] args) throws IOException {
		FeaturesTokenizer tokenizer = new FeaturesTokenizer();
		tokenizer.setReader(new StringReader("p1:v2;p2;p3"));
		tokenizer.reset();
		while(tokenizer.incrementToken()){
			System.out.println(new String(tokenizer.termAtt.buffer(),0,tokenizer.termAtt.length()));
		}
	}
}
