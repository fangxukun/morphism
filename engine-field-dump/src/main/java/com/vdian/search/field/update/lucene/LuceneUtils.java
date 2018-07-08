package com.vdian.search.field.update.lucene;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.FieldInfosFormat;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.RefCounted;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 17/4/11
 * Time: 17:50
 */
public class LuceneUtils {

	public static Map<String,FieldInfo> extractFieldInfo(IndexWriter writer) throws IOException {
		Directory directory = writer.getDirectory();
		SegmentInfos segmentInfos = SegmentInfos.readLatestCommit(directory);
		Map<String,FieldInfo> result = new HashMap<>();

		for(SegmentCommitInfo segmentInfo : segmentInfos){
			FieldInfos fis = readFieldInfos(segmentInfo);
			for(FieldInfo fi : fis){
				result.put(fi.name,fi);
			}
		}

		return result;
	}


	/**
	 * Copy from IndexWriter
	 * @param si
	 * @return
	 * @throws IOException
	 */
	private static FieldInfos readFieldInfos(SegmentCommitInfo si) throws IOException {
		Codec codec = si.info.getCodec();
		FieldInfosFormat reader = codec.fieldInfosFormat();

		if (si.hasFieldUpdates()) {
			final String segmentSuffix = Long.toString(si.getFieldInfosGen(), Character.MAX_RADIX);
			return reader.read(si.info.dir, si.info, segmentSuffix, IOContext.READONCE);
		} else if (si.info.getUseCompoundFile()) {
			try (Directory cfs = codec.compoundFormat().getCompoundReader(si.info.dir, si.info, IOContext.DEFAULT)) {
				return reader.read(cfs, si.info, "", IOContext.READONCE);
			}
		} else {
			return reader.read(si.info.dir, si.info, "", IOContext.READONCE);
		}
	}


	public static Map<String,FieldInfo> getFieldInfo(SolrCore core) throws IOException {
		RefCounted<IndexWriter> iw = null;
		try{
			iw = core.getSolrCoreState().getIndexWriter(core);
			return LuceneUtils.extractFieldInfo(iw.get());
		}finally {
			if(iw != null){
				iw.decref();
			}
		}
	}
}
