package com.vdian.search.ann.lucene.codecs;

import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.codecs.lucene50.Lucene50DocValuesFormat;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 12:27
 */
public class AnnDocValuesFormat extends DocValuesFormat {
	public static final String 			DATA_CODEC				=	"AnnData";
	public static final String 			META_CODEC				=	"AnnMeta";

	public static final String 			META_EXTENSION			=	"anm";
	public static final String 			DATA_EXTENSION			=	"and";

	public static final int 			META_VERSION_CURRENT	=	0;
	public static final int 			DATA_VERSION_CURRENT	=	0;

	public static final long 			NONE					=	0l;
	public static final int				FIELD_FLAG				=	1;

	private final DocValuesFormat		delegate 	=	new Lucene50DocValuesFormat();


	public AnnDocValuesFormat(){
		super("ann");
	}


	@Override
	public DocValuesConsumer fieldsConsumer(SegmentWriteState state) throws IOException {
		return new AnnDocValuesConsumer(state,delegate.fieldsConsumer(state));
	}

	@Override
	public DocValuesProducer fieldsProducer(SegmentReadState state) throws IOException {
		return new AnnDocValuesProducer(state,delegate.fieldsProducer(state));
	}
}
