//package com.vdian.engine.ann.lucene;
//
//import com.google.common.base.Stopwatch;
//import com.google.common.io.Resources;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.koudai.rio.commons.utils.GsonUtils;
//import com.vdian.search.ann.BytesUtils;
//import com.vdian.search.ann.Item;
//import com.vdian.search.ann.algorithm.FloatCalculator;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.common.SolrInputDocument;
//import org.apache.solr.core.CoreContainer;
//import org.apache.solr.core.SolrCore;
//import org.apache.solr.request.SolrQueryRequest;
//import org.apache.solr.request.SolrRequestHandler;
//import org.apache.solr.response.JSONResponseWriter;
//import org.apache.solr.response.SolrQueryResponse;
//import org.apache.solr.servlet.SolrRequestParsers;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.io.StringWriter;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * User: xukun.fyp
// * Date: 17/3/9
// * Time: 20:24
// */
//public class SolrAnnCore {
//	private SolrCore 				core;
//
//	public SolrAnnCore(boolean rebuild) throws IOException {
//		if(rebuild){
//			Supporter.deleteIfExist("/data/data_home/item/");
//		}
//		CoreContainer container = new CoreContainer(Resources.getResource("solr-home").getPath());
//		container.load();
//		this.core = container.getCore("item");
//	}
//
//	public void addItem(Item item) throws IOException {
//		SolrInputDocument doc = newDoc(item.id,item.vector);
//		Indexer.addDocument(core,doc);
//	}
//
//	public void buildIndex(int numOfTree) throws IOException {
//		Indexer.commit(core);
//	}
//
//	public List<float[]> search(float[] queryVector,int searchNum,int returnNum,Stopwatch sw) throws Exception {
//		SolrRequestHandler handler = core.getRequestHandler("/select");
//		SolrQuery query = new SolrQuery();
//		query.setQuery("*:*");
//		query.setFilterQueries(String.format("{!ann field=imgVector searchNum=%s}", searchNum));
//		query.set("vector", FloatCalculator.strFromFloatVector(queryVector));
//		query.addSort("annDistance(imgVector)", SolrQuery.ORDER.asc);
//		query.setRows(returnNum);
//		SolrQueryRequest request = SolrRequestParsers.DEFAULT.buildRequestFrom(core,query,null);
//		SolrQueryResponse response = new SolrQueryResponse();
//		sw.start();
//		core.execute(handler, request, response);
//		sw.stop();
//
//		JSONResponseWriter writer = new JSONResponseWriter();
//		StringWriter stringWriter = new StringWriter();
//		writer.write(stringWriter, request, response);
//
//		List<float[]> result = new ArrayList<>();
//		JsonObject root = GsonUtils.parseJsonObject(stringWriter.toString()).getAsJsonObject();
//		JsonArray docs = root.get("response").getAsJsonObject().get("docs").getAsJsonArray();
//		for(JsonElement doc: docs){
//			String v = doc.getAsJsonObject().get("imgVector").getAsString();
//			float[] imgVector = BytesUtils.fromBase64Str(v);
//			result.add(imgVector);
//		}
//		return result;
//	}
//
//	private SolrInputDocument newDoc(long id,float[] vector){
//		SolrInputDocument doc = new SolrInputDocument();
//		doc.setField("imgVector", BytesUtils.vectorToBytes(vector));
//		doc.setField("_biz_version_",System.currentTimeMillis());
//		doc.setField("item_id",id);
//		doc.setField("seller_id", RandomUtils.nextInt(1000));
//		return doc;
//	}
//
//
//
//}
