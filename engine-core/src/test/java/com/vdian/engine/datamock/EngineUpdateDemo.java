package com.vdian.engine.datamock;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 16/12/3
 * Time: 13:53
 */
public class EngineUpdateDemo {
	private HttpSolrClient 	writeClient;

	private int 			tolerance = 0;
	private String[] 		fields = new String[]{"item_title", "status", "shield_v2"};

	@Before
	public void setup() {
		this.writeClient = new HttpSolrClient("http://localhost:8080/xukun_demo/");
	}

	@Test
	public void updateField() throws IOException, SolrServerException {
		updateFields(Pair.of("item_id",1),Pair.of("status",12));
	}

	public boolean updateFields(Pair<String,?> key,Pair<String,?> ... fields) throws IOException, SolrServerException {
		SolrInputDocument document = new SolrInputDocument();
		document.addField(key.getKey(), key.getValue());

		for(Pair<String,?> field : fields){
			Map<String,Object> updateMap = new HashMap<>();
			updateMap.put("set",field.getValue());
			document.addField(field.getKey(),updateMap);
		}

		UpdateResponse response = this.writeClient.add(document,1000);
		return response.getStatus() == 0;
	}
}
