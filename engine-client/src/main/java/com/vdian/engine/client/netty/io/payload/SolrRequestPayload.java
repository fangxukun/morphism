//package com.vdian.engine.client.netty.io.payload;
//
//import com.koudai.rio.commons.io.NeuronReader;
//import com.koudai.rio.commons.io.NeuronWriter;
//import org.apache.solr.client.solrj.SolrRequest;
//import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
//import org.apache.solr.client.solrj.request.RequestWriter;
//import org.apache.solr.client.solrj.request.UpdateRequest;
//import org.apache.solr.common.params.CommonParams;
//import org.apache.solr.common.params.ModifiableSolrParams;
//import org.apache.solr.common.params.SolrParams;
//import org.apache.solr.common.util.ContentStream;
//import org.apache.solr.common.util.ContentStreamBase;
//import org.omg.CORBA.PRIVATE_MEMBER;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//
///**
// * User: xukun.fyp
// * Date: 16/12/14
// * Time: 11:16
// */
//public class SolrRequestPayload implements RequestPayload {
//	private SolrParams 					solrParams;
//	private String 						collection;
//	private String 						path;
//	private SolrRequest					request;
//
//	public SolrRequestPayload(SolrParams params, String collection, String path) {
//		this.solrParams = params;
//		this.collection = collection;
//		this.path = path;
//	}
//
//	public SolrRequestPayload(UpdateRequest request,String collection,String path){
//		try{
//			this.solrParams = request.getParams();
//			this.collection = collection;
//			this.path = path;
//			this.request = request;
//		}catch (Exception e){
//			e.printStackTrace();//TODO:
//		}
//	}
//
//	@Override
//	public void reset() {
//
//	}
//
//	public SolrRequestPayload() {
//		this.solrParams = new ModifiableSolrParams();
//	}
//
//	@Override
//	public byte getPayloadId() {
//		return PAYLOAD_ID_SOLR_REQUEST;
//	}
//
//	@Override
//	public void readFields(NeuronReader reader) throws IOException {
//		ModifiableSolrParams solrParams = (ModifiableSolrParams) this.solrParams;
//		solrParams.clear();
//
//		int length = reader.readSVInt();
//		for (int i = 0; i < length; i++) {
//			String key = reader.readString();
//			int valLength = reader.readSVInt();
//			String[] values = new String[valLength];
//			for (int j = 0; j < valLength; j++) {
//				values[j] = reader.readString();
//			}
//			solrParams.add(key, values);
//		}
//		this.collection = reader.readString();
//		this.path = reader.readString();
//
//	}
//
//	@Override
//	public void write(NeuronWriter writer) throws IOException {
//		writer.writeSVInt(lengthOfIterator(solrParams.getParameterNamesIterator()));
//
//		Iterator<String> keyIterator = solrParams.getParameterNamesIterator();
//		while (keyIterator.hasNext()) {
//			String key = keyIterator.next();
//			String[] values = solrParams.getParams(key);
//
//			writer.writeString(key);
//			writer.writeSVInt(values.length);
//			for (String val : values) {
//				writer.writeString(val);
//			}
//		}
//
//		writer.writeString(collection);
//		writer.writeString(path);
//
//		RequestWriter rw = new BinaryRequestWriter();
//		rw.getContentStreams(request);
//
//	}
//
//	private int lengthOfIterator(Iterator<String> iterator) {
//		int length = 0;
//		while (iterator.hasNext()) {
//			iterator.next();
//			length++;
//		}
//		return length;
//	}
//
//	@Override
//	public long getRequestTimeout(long defaultTimeout) {
//		return solrParams.getLong(CommonParams.TIME_ALLOWED,defaultTimeout);
//	}
//
//	public SolrParams getSolrParams() {
//		return solrParams;
//	}
//
//	public String getCollection() {
//		return collection;
//	}
//
//	public String getPath() {
//		return path;
//	}
//
//	@Override
//	public String toString() {
//		return "SolrRequestPayload{" +
//				"solrParams=" + solrParams +
//				", collection='" + collection + '\'' +
//				", path='" + path + '\'' +
//				'}';
//	}
//}
