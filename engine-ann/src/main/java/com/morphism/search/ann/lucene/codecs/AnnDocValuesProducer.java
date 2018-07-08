package com.morphism.search.ann.lucene.codecs;

import com.morphism.search.ann.AnnLayout;
import com.morphism.search.ann.DataType;
import com.morphism.search.ann.lucene.AnnLayouts;
import com.morphism.search.ann.lucene.codecs.floats.AnnFloatTreeReader;
import com.morphism.search.ann.lucene.codecs.shorts.AnnShortTreeReader;
import com.morphism.search.ann.lucene.codecs.bytes.AnnByteTreeReader;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 12:29
 */
public class AnnDocValuesProducer extends DocValuesProducer {
	private final DocValuesProducer				delegate;
	private final AtomicLong 					ramBytesUsed;
	private final IndexInput 					data;
	private final int 							maxDoc;
	private final boolean 						merging;
	private final MetaCodec						metaCodec;

	public AnnDocValuesProducer(AnnDocValuesProducer orig) throws IOException{
		data = orig.data.clone();
		ramBytesUsed = new AtomicLong(orig.ramBytesUsed.get());
		delegate = orig.delegate.getMergeInstance();
		maxDoc = orig.maxDoc;
		metaCodec = orig.metaCodec;
		merging = true;
	}

	public AnnDocValuesProducer(SegmentReadState state,DocValuesProducer delegate) throws IOException{
		this.maxDoc = state.segmentInfo.maxDoc();
		this.delegate = delegate;
		this.merging = false;
		this.ramBytesUsed = new AtomicLong(RamUsageEstimator.shallowSizeOfInstance(getClass()));
		this.metaCodec = new MetaCodec();

		//1. read meta
		String metaName = IndexFileNames.segmentFileName(state.segmentInfo.name,state.segmentSuffix,AnnDocValuesFormat.META_EXTENSION);
		try(ChecksumIndexInput in = state.directory.openChecksumInput(metaName,state.context)){
			Throwable priorE = null;
			try{
				CodecUtil.checkIndexHeader(in,
						AnnDocValuesFormat.META_CODEC,
						AnnDocValuesFormat.META_VERSION_CURRENT,
						AnnDocValuesFormat.META_VERSION_CURRENT,
						state.segmentInfo.getId(),
						state.segmentSuffix);

				metaCodec.readFields(in);
				fillAnnLayout();
			}catch (Throwable r){
				priorE = r;
			}finally {
				CodecUtil.checkFooter(in,priorE);
			}
		}

		//2. data
		boolean success = false;
		String dataName = IndexFileNames.segmentFileName(state.segmentInfo.name,state.segmentSuffix, AnnDocValuesFormat.DATA_EXTENSION);
		this.data = state.directory.openInput(dataName,state.context);
		try{
			CodecUtil.checkIndexHeader(
					data,
					AnnDocValuesFormat.DATA_CODEC,
					AnnDocValuesFormat.DATA_VERSION_CURRENT,
					AnnDocValuesFormat.DATA_VERSION_CURRENT,
					state.segmentInfo.getId(),
					state.segmentSuffix
			);

			CodecUtil.retrieveChecksum(data);
			success = true;
		}finally {
			if(!success){
				IOUtils.closeWhileHandlingException(this.data);
			}
		}
	}

	@Override
	public BinaryDocValues getBinary(FieldInfo field) throws IOException {
		AnnLayout layout = AnnLayouts.getLayout(field.name);
		BinaryDocValues originValue = delegate.getBinary(field);

		System.err.println(String.format("getBinary,field:%s,layout:%s",field.name,layout));

		AnnTreeReader reader;
		if(layout.dataType == DataType.BYTE){
			reader = new AnnByteTreeReader(data,metaCodec.getField(field.name),layout,originValue);
		}else if(layout.dataType == DataType.SHORT){
			reader = new AnnShortTreeReader(data,metaCodec.getField(field.name),layout,originValue);
		}else{
			reader = new AnnFloatTreeReader(data,metaCodec.getField(field.name),layout,originValue);
		}
		return new AnnBinaryDocValues(reader);
	}

	private void fillAnnLayout(){
		for(MetaCodec.FieldCodec fieldCodec : metaCodec.fieldCodecs){
			AnnLayout layout = new AnnLayout(fieldCodec.numOfTree,200,64,fieldCodec.dataType,false);
			System.err.println("layout from meta codec,layout:" + layout);

			if(AnnLayouts.notExist(fieldCodec.fieldName)){
				System.err.println("register layout success!");
				AnnLayouts.register(fieldCodec.fieldName,layout);
			}
		}
	}


	@Override
	public Bits getDocsWithField(FieldInfo field) throws IOException {
		return delegate.getDocsWithField(field);
	}

	@Override
	public void checkIntegrity() throws IOException {
		CodecUtil.checksumEntireFile(data);
	}

	@Override
	public long ramBytesUsed() {
		return ramBytesUsed.get() + delegate.ramBytesUsed();
	}

	@Override
	public Collection<Accountable> getChildResources() {
		List<Accountable> resources = new ArrayList<>();
		resources.add(Accountables.namedAccountable("delegate", delegate));
		return resources;
	}

	@Override
	public void close() throws IOException {
		IOUtils.close(data,delegate);
	}

	@Override
	public DocValuesProducer getMergeInstance() throws IOException {
		return new AnnDocValuesProducer(this);
	}

	@Override
	public NumericDocValues getNumeric(FieldInfo field) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedDocValues getSorted(FieldInfo field) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedNumericDocValues getSortedNumeric(FieldInfo field) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSetDocValues getSortedSet(FieldInfo field) throws IOException {
		throw new UnsupportedOperationException();
	}
}
