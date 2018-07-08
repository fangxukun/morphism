package com.vdian.engine.datamock;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 16/12/2
 * Time: 18:11
 */
public class EngineDataMock {

	private HttpSolrClient 	queryClient;
	private HttpSolrClient 	writeClient;
	private DocIterator 	docIterator;

	private int 			tolerance = 0;
	private String[] 		remoteFields = new String[]{"item_id", "price", "item_title","audition_score","_biz_version_"};
	private String[] 		fields = new String[]{"item_id", "price", "title","score","_biz_version_"};

	@Before
	public void setup() {
		this.queryClient = new HttpSolrClient("http://10.2.106.33:8080/vitem/");
		this.writeClient = new HttpSolrClient("http://localhost:8080/hilbert/");

		this.docIterator = new DocIterator();
	}

	@Test	
	public void insert10W() throws IOException, SolrServerException {
		for (int i = 0; i < 10*1000 ; i++) {
			try{
				UpdateResponse response = writeClient.add(newDocument(i));
				Preconditions.checkArgument(response.getStatus() == 0);
			}catch (Exception e){
				if(tolerance++ > 1){
					throw new RuntimeException("tolerance exhaust!");
				}
				e.printStackTrace();
			}
		}
		writeClient.commit();
	}


	private SolrInputDocument newDocument(int id) {
		SolrInputDocument input = new SolrInputDocument();
		input.addField("item_id", id);

		SolrDocument document = this.docIterator.next();
		for (int i=0;i<remoteFields.length;i++) {
			input.setField(fields[i],document.get(remoteFields[i]));
		}
		return input;
	}

	@Test
	public void testPerformance() throws IOException, SolrServerException {
		int start = 0;
		int rows = 10;
		int loop = 50;
		String q = "item_title:T恤女";

		Stopwatch watch = Stopwatch.createStarted();
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(rows);
			query.setFields("item_id");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("1.row:10,fl:item_id==>t\t" + watch.elapsed(TimeUnit.MILLISECONDS));
		watch.reset().start();

		start = 0;
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(rows);
			query.setFields("*");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("2.row:10,fl:*==>\t" + watch.elapsed(TimeUnit.MILLISECONDS));
		watch.reset().start();

		start = 0;
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(rows);
			query.setFields("item_id");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("1.row:10,fl:item_id==>t\t" + watch.elapsed(TimeUnit.MILLISECONDS));

		start = 0;
		rows = 200;
		watch = Stopwatch.createStarted();
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(rows);

			query.setFields("item_id");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("3.row:200,fl:item_id==>t\t" + watch.elapsed(TimeUnit.MILLISECONDS));
		watch.reset().start();

		start = 0;
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(10);
			query.setFields("*");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("4.row:200,fl:*==>\t" + watch.elapsed(TimeUnit.MILLISECONDS));

		start = 0;
		for(int i=0;i<loop;i++){
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(start);
			query.setRows(10);
			query.setFields("item_id");
			QueryResponse response = queryClient.query(query);
			start += rows;
		}
		System.out.println("4.row:200,fl:*==>\t" + watch.elapsed(TimeUnit.MILLISECONDS));
	}


	public class DocIterator implements Iterator<SolrDocument> {
		private int cursor = 0;
		private SolrDocumentList buffered;
		private int start = 0;


		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public SolrDocument next() {
			if (buffered == null || buffered.size() == cursor) {
				try {
					cursor = 0;
					SolrQuery query = new SolrQuery();
					query.setQuery("*:*");
					query.setStart(start);
					query.setRows(100);
					query.setFields(remoteFields).addField("item_id");
					QueryResponse response = queryClient.query(query);
					this.buffered = response.getResults();
					if (buffered.size() == 0) {
						throw new RuntimeException("End of Query!");
					}
					start += this.buffered.size();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			return buffered.get(cursor++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}




}
