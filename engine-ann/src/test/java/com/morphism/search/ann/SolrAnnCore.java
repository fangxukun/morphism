package com.morphism.search.ann;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koudai.rio.commons.utils.GsonUtils;
import com.morphism.search.ann.algorithm.FloatCalculator;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.lucene.store.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.SolrRequestParsers;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 20:24
 */
public class SolrAnnCore {
	private SolrCore 				core;
	private int 					count;

	public SolrAnnCore(boolean rebuild) throws Exception {
		if(rebuild){
			Supporter.deleteIfExist("/data/data_home/item/");
			NativeFSLockFactory factory = NativeFSLockFactory.INSTANCE;
			Lock lock = factory.obtainLock(new SimpleFSDirectory(Paths.get("/data/data_home/vision/index/")), "write.lock");
			Field field = lock.getClass().getDeclaredField("LOCK_HELD");
			field.setAccessible(true);
			((Set<String>)field.get(lock)).clear();
		}
		CoreContainer container = new CoreContainer(Resources.getResource("solr-home").getPath());
		container.load();
		this.core = container.getCore("item");
	}

	public void addItem(Item item) throws IOException {
		SolrInputDocument doc = newDoc(item.getId(),item.getVector());
		Indexer.addDocument(core,doc);
		if(++count % 200000 == 0){
			System.out.println("commit core,count:" + count);
			Indexer.commit(core);
		}
	}

	public void buildIndex() throws IOException {
		Indexer.commit(core);
	}

	public List<float[]> search(float[] queryVector,int searchNum,int returnNum,Stopwatch sw) throws Exception {
		SolrRequestHandler handler = core.getRequestHandler("/select");
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		query.setFilterQueries(String.format("{!ann field=imgVector searchNum=%s}", searchNum));
		query.set("vector", FloatCalculator.strFromFloatVector(queryVector));
		query.addSort("annDistance(imgVector)", SolrQuery.ORDER.asc);
		query.setRows(returnNum);
		SolrQueryRequest request = SolrRequestParsers.DEFAULT.buildRequestFrom(core,query,null);
		SolrQueryResponse response = new SolrQueryResponse();
		sw.start();
		core.execute(handler, request, response);
		sw.stop();

		JSONResponseWriter writer = new JSONResponseWriter();
		StringWriter stringWriter = new StringWriter();
		writer.write(stringWriter, request, response);

		List<float[]> result = new ArrayList<>();
		JsonObject root = GsonUtils.parseJsonObject(stringWriter.toString()).getAsJsonObject();
		JsonArray docs = root.get("response").getAsJsonObject().get("docs").getAsJsonArray();
		for(JsonElement doc: docs){
			JsonArray v = doc.getAsJsonObject().get("imgVector").getAsJsonArray();
			float[] imgVector = new float[v.size()];
			for(int i=0;i<imgVector.length;i++){
				imgVector[i] = v.get(i).getAsFloat();
			}
			result.add(imgVector);
		}
		return result;
	}

	public void close(){
		this.core.closeSearcher();
		this.core.close();
	}




	private SolrInputDocument newDoc(long id,float[] vector){
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("imgVector", BytesUtils.floatsToBytes(vector));
		doc.setField("_biz_version_",System.currentTimeMillis());
		doc.setField("item_id",id);
		doc.setField("seller_id", RandomUtils.nextInt(1000));
		return doc;
	}
}
