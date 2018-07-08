package com.vdian.search.ann;

import com.vdian.search.ann.lucene.codecs.AnnDocValuesFormat;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.lucene50.Lucene50Codec;
import org.apache.lucene.codecs.lucene50.Lucene50DocValuesFormat;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;
import org.apache.solr.core.SchemaCodecFactory;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.apache.solr.schema.SchemaField;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/3/20
 * Time: 16:35
 */
public class IndexerMerger {

	@Test
	public void forceMerge() throws IOException {
		IndexWriterConfig iwc = new IndexWriterConfig(null);
		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		iwc.setCodec(new Lucene50Codec(){
			@Override
			public DocValuesFormat getDocValuesFormatForField(String field) {
				if(field.equals("imgVector")){
					return new AnnDocValuesFormat();
				}else{
					return new Lucene50DocValuesFormat();
				}
			}
		});

		MMapDirectory d0 = new MMapDirectory(Paths.get("/data/data_home/item/00"));
		IndexWriter writer = new IndexWriter(d0,iwc);

		MMapDirectory d1 = new MMapDirectory(Paths.get("/data/data_home/item/01/index"));
		writer.addIndexes(d1);

		MMapDirectory d2 = new MMapDirectory(Paths.get("/data/data_home/item/02/index"));
		writer.addIndexes(d2);

		MMapDirectory d3 = new MMapDirectory(Paths.get("/data/data_home/item/03/index"));
		writer.addIndexes(d3);

		MMapDirectory d4 = new MMapDirectory(Paths.get("/data/data_home/item/04/index"));
		writer.addIndexes(d4);

		MMapDirectory d5 = new MMapDirectory(Paths.get("/data/data_home/item/05/index"));
		writer.addIndexes(d5);

		MMapDirectory d6 = new MMapDirectory(Paths.get("/data/data_home/item/06/index"));
		writer.addIndexes(d6);

		writer.forceMerge(3);
		writer.close();
	}
}
