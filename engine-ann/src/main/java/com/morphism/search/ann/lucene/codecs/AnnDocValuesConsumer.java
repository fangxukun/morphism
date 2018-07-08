package com.morphism.search.ann.lucene.codecs;

import com.morphism.search.ann.AnnLayout;
import com.morphism.search.ann.DataType;
import com.morphism.search.ann.lucene.AnnLayouts;
import com.morphism.search.ann.lucene.codecs.bytes.AnnByteTreeWriter;
import com.morphism.search.ann.lucene.codecs.floats.AnnFloatTreeWriter;
import com.morphism.search.ann.lucene.codecs.shorts.AnnShortTreeWriter;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 12:29
 */
public class AnnDocValuesConsumer extends DocValuesConsumer implements Closeable {
	public final SegmentWriteState 			state;
	public final DocValuesConsumer			delegate;

	public final IndexOutput 				meta;
	public final IndexOutput				data;
	public final MetaCodec					metaCodec;

	private boolean 						closed	=	false;


	public AnnDocValuesConsumer(SegmentWriteState state, DocValuesConsumer delegate) throws IOException{
		this.delegate = delegate;
		this.state = state;

		boolean success = false;
		try{
			String metaName = IndexFileNames.segmentFileName(state.segmentInfo.name, state.segmentSuffix, AnnDocValuesFormat.META_EXTENSION);
			meta = state.directory.createOutput(metaName,state.context);
			CodecUtil.writeIndexHeader(meta, AnnDocValuesFormat.META_CODEC, AnnDocValuesFormat.META_VERSION_CURRENT,state.segmentInfo.getId(),state.segmentSuffix);
			metaCodec = new MetaCodec();

			String dataFile = IndexFileNames.segmentFileName(state.segmentInfo.name,state.segmentSuffix, AnnDocValuesFormat.DATA_EXTENSION);
			data = state.directory.createOutput(dataFile,state.context);
			CodecUtil.writeIndexHeader(data, AnnDocValuesFormat.DATA_CODEC, AnnDocValuesFormat.DATA_VERSION_CURRENT,state.segmentInfo.getId(),state.segmentSuffix);
			success = true;
		}finally {
			if(!success){
				IOUtils.closeWhileHandlingException(this);
			}
		}
	}

	@Override
	public void addBinaryField(FieldInfo field, Iterable<BytesRef> values) throws IOException {
		AnnLayout layout = AnnLayouts.getLayout(field.name);

		System.err.println(String.format("addBinaryField,field:%s,layout:%s",field.name,layout));

		AnnTreeWriter treeWriter = null;
		if(layout.dataType == DataType.BYTE){
			treeWriter = new AnnByteTreeWriter(this);
		}else if(layout.dataType == DataType.SHORT){
			treeWriter = new AnnShortTreeWriter(this);
		}else{
			treeWriter = new AnnFloatTreeWriter(this);
		}

		treeWriter.addBinaryField(field,values,layout);
	}


	@Override
	public void close() throws IOException {
		boolean success = false;
		try{
			if(closed == false){
				metaCodec.write(meta);

				CodecUtil.writeFooter(meta);
				CodecUtil.writeFooter(data);
			}
			success = true;
		}finally {
			if(success){
				IOUtils.close(delegate, data, meta);
			}else{
				IOUtils.closeWhileHandlingException(delegate,data,meta);
			}
			closed = true;
		}
	}

	@Override
	public void addNumericField(FieldInfo field, Iterable<Number> values) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addSortedField(FieldInfo field, Iterable<BytesRef> values, Iterable<Number> docToOrd) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addSortedNumericField(FieldInfo field, Iterable<Number> docToValueCount, Iterable<Number> values) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addSortedSetField(FieldInfo field, Iterable<BytesRef> values, Iterable<Number> docToOrdCount, Iterable<Number> ords) throws IOException {
		throw new UnsupportedOperationException();
	}
}
