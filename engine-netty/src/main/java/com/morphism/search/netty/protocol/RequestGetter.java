package com.morphism.search.netty.protocol;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/27
 * Time: 14:49
 */
public class RequestGetter {
	private SolrProtocol.NettyRequest 		request;

	private final ModifiableSolrParams		solrParams;

	public RequestGetter() {
		this.solrParams = new ModifiableSolrParams();
	}

	public void reset(SolrProtocol.NettyRequest request) {
		this.request = request;
		this.solrParams.clear();

		this.extractSolrParams();
	}


	/**
	 * @return connect id;
	 */
	public long getCid(){
		return request.getCid();
	}

	/**
	 * @return request id
	 */
	public long getRid(){
		return request.getRid();
	}


	public SolrParams getSolrParams(){
		return solrParams;
	}

	public String getWriteType(){
		return solrParams.get(CommonParams.WT, CommonParams.JAVABIN);
	}

	public String getCollection(){
		return request.getSolrRequest().getCollection();
	}

	public String getPath(){
		return request.getSolrRequest().getPath();
	}

	public Collection getContentStreamList(){
		Collection<ContentStream> streams = new ArrayList<>();
		for(SolrProtocol.ContentStream protoStream : request.getSolrRequest().getContentStreamList()){
			streams.add(new ContentStreamWrap(protoStream));
		}

		return streams;
	}

	public void extractSolrParams(){
		List<SolrProtocol.SolrParam> paramList = request.getSolrRequest().getSolrParamList();
		for(SolrProtocol.SolrParam param : paramList){
			List<String> values = param.getValuesList();

			if(values == null){
				solrParams.add(param.getKey(),null);
			}else{
				solrParams.add(param.getKey(),values.toArray(new String[values.size()]));
			}
		}
	}

}
