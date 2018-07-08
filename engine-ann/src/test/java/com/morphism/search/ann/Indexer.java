package com.morphism.search.ann;

import com.google.common.io.Resources;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;

import java.io.IOException;


/**
 * User: xukun.fyp
 * Date: 17/3/8
 * Time: 15:32
 */
public class Indexer {

	public static SolrCore initSolrCore(String coreName){
		CoreContainer container = new CoreContainer(Resources.getResource("solr-home").getPath());
		container.load();
		return container.getCore(coreName);
	}

	public static void addDocument(SolrCore core,SolrInputDocument document) throws IOException {
		SolrQueryRequest req = new LocalSolrQueryRequest(core, (SolrParams) null);
		AddUpdateCommand cmd = new AddUpdateCommand(req);
		cmd.solrDoc = document;
		core.getUpdateHandler().addDoc(cmd);
	}

	public static void commit(SolrCore core) throws IOException {
		SolrQueryRequest req = new LocalSolrQueryRequest(core, (SolrParams) null);
		CommitUpdateCommand cmd = new CommitUpdateCommand(req,true);
		core.getUpdateHandler().commit(cmd);
	}


	private byte[] randomVectors(int d){
		float[] f = new float[d];
		for(int i=0;i<d;i++){
			f[i] = RandomUtils.nextFloat();
		}
		return BytesUtils.floatsToBytes(f);
	}
	private String strRandomVectors(int d){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<d;i++){
			if(sb.length() > 0){
				sb.append(",");
			}
			sb.append(RandomUtils.nextFloat());
		}
		return sb.toString();
	}


	private SolrInputDocument toInputDocument(String[] fields,SolrDocument document){
		SolrInputDocument input = new SolrInputDocument();

		for (String field: fields) {
			input.setField(field,document.get(field));
		}
		input.setField("_biz_version_",System.currentTimeMillis());
		return input;
	}

}
