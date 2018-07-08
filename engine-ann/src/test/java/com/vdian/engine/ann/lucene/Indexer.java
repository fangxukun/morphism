//package com.vdian.engine.ann.lucene;
//
//import com.google.common.io.Resources;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.koudai.rio.commons.utils.GsonUtils;
//import BytesUtils;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.impl.HttpSolrClient;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.SolrDocument;
//import org.apache.solr.common.SolrDocumentList;
//import org.apache.solr.common.SolrInputDocument;
//import org.apache.solr.common.params.SolrParams;
//import org.apache.solr.core.CoreContainer;
//import org.apache.solr.core.SolrCore;
//import org.apache.solr.request.LocalSolrQueryRequest;
//import org.apache.solr.request.SolrQueryRequest;
//import org.apache.solr.request.SolrRequestHandler;
//import org.apache.solr.response.JSONResponseWriter;
//import org.apache.solr.response.ResultContext;
//import org.apache.solr.response.SolrQueryResponse;
//import org.apache.solr.search.SolrIndexSearcher;
//import org.apache.solr.servlet.SolrRequestParsers;
//import org.apache.solr.update.AddUpdateCommand;
//import org.apache.solr.update.CommitUpdateCommand;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.io.StringWriter;
//import java.util.Iterator;
//import java.util.Objects;
//
//
///**
// * User: xukun.fyp
// * Date: 17/3/8
// * Time: 15:32
// */
//public class Indexer {
//
//	public static SolrCore initSolrCore(String coreName){
//		CoreContainer container = new CoreContainer(Resources.getResource("solr-home").getPath());
//		container.load();
//		return container.getCore(coreName);
//	}
//
//	public static void addDocument(SolrCore core,SolrInputDocument document) throws IOException {
//		SolrQueryRequest req = new LocalSolrQueryRequest(core, (SolrParams) null);
//		AddUpdateCommand cmd = new AddUpdateCommand(req);
//		cmd.solrDoc = document;
//		core.getUpdateHandler().addDoc(cmd);
//	}
//
//	public static void commit(SolrCore core) throws IOException {
//		SolrQueryRequest req = new LocalSolrQueryRequest(core, (SolrParams) null);
//		CommitUpdateCommand cmd = new CommitUpdateCommand(req,true);
//		core.getUpdateHandler().commit(cmd);
//	}
//
//	@Test
//	public void addAnnIndex() throws IOException {
//		SolrCore core = initSolrCore("item");
//		int count = 100;
//
//		String fields = "item_id,seller_id";
//		DocIterator iterator = new DocIterator(fields,new HttpSolrClient("http://10.2.106.33:8080/vitem/"));
//
//		for(int i=0;i<count;i++){
//			SolrInputDocument inputDoc = toInputDocument(fields.split(","),iterator.next());
//			inputDoc.addField("imgVector",randomVectors(40));
//			addDocument(core,inputDoc);
//			if(i%100 == 0){
//				System.out.println("add doc:" + i);
//			}
//		}
//		commit(core);
//	}
//
//	private byte[] randomVectors(int d){
//		float[] f = new float[d];
//		for(int i=0;i<d;i++){
//			f[i] = RandomUtils.nextFloat();
//		}
//		return BytesUtils.vectorToBytes(f);
//	}
//	private String strRandomVectors(int d){
//		StringBuilder sb = new StringBuilder();
//		for(int i=0;i<d;i++){
//			if(sb.length() > 0){
//				sb.append(",");
//			}
//			sb.append(RandomUtils.nextFloat());
//		}
//		return sb.toString();
//	}
//
//	@Test
//	public void queryAnnIndex() throws Exception {
//
//		SolrCore core = initSolrCore("item");
//		SolrRequestHandler handler = core.getRequestHandler("/select");
//		SolrQuery query = new SolrQuery();
//		query.setQuery("*:*");
//		query.setFilterQueries("{!ann field=imgVector searchNum=500}");
//		query.set("vector", strRandomVectors(2048));
//		query.addSort("annDistance(imgVector)", SolrQuery.ORDER.asc);
//		SolrQueryRequest request = SolrRequestParsers.DEFAULT.buildRequestFrom(core,query,null);
//		SolrQueryResponse response = new SolrQueryResponse();
//		core.execute(handler, request, response);
//
//		JSONResponseWriter writer = new JSONResponseWriter();
//		StringWriter stringWriter = new StringWriter();
//		writer.write(stringWriter, request, response);
//
//		JsonObject root = GsonUtils.parseJsonObject(stringWriter.toString()).getAsJsonObject();
//
//		JsonArray docs = root.get("response").getAsJsonObject().get("docs").getAsJsonArray();
//		for(JsonElement doc: docs){
//			String v = doc.getAsJsonObject().get("imgVector").getAsString();
//			float[] tt = BytesUtils.fromBase64Str(v);
//			System.out.println(tt);
//		}
//
//		System.out.println(stringWriter.toString());
//	}
//
//	private SolrInputDocument toInputDocument(String[] fields,SolrDocument document){
//		SolrInputDocument input = new SolrInputDocument();
//
//		for (String field: fields) {
//			input.setField(field,document.get(field));
//		}
//		input.setField("_biz_version_",System.currentTimeMillis());
//		return input;
//	}
//
//	public class DocIterator implements Iterator<SolrDocument> {
//		private int cursor = 0;
//		private SolrDocumentList buffered;
//		private int start = 0;
//		private String 		returnField;
//		private HttpSolrClient	queryClient;
//
//		public DocIterator(String returnField,HttpSolrClient queryClient){
//			this.returnField = returnField;
//			this.queryClient = queryClient;
//		}
//
//		@Override
//		public boolean hasNext() {
//			return true;
//		}
//
//		@Override
//		public SolrDocument next() {
//			if (buffered == null || buffered.size() == cursor) {
//				try {
//					cursor = 0;
//					SolrQuery query = new SolrQuery();
//					query.setQuery("*:*");
//					query.setStart(start);
//					query.setRows(100);
//					query.setFields(returnField).addField("item_id");
//					QueryResponse response = queryClient.query(query);
//					this.buffered = response.getResults();
//					if (buffered.size() == 0) {
//						throw new RuntimeException("End of Query!");
//					}
//					start += this.buffered.size();
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//
//			return buffered.get(cursor++);
//		}
//
//		@Override
//		public void remove() {
//			throw new UnsupportedOperationException();
//		}
//	}
//}
