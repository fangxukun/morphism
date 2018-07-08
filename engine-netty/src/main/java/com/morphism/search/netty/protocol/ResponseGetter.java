package com.morphism.search.netty.protocol;

import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.common.util.NamedList;

import java.io.InputStream;

/**
 * User: xukun.fyp
 * Date: 17/3/28
 * Time: 14:29
 */
public class ResponseGetter {
	private BinaryResponseParser			parser		=	new BinaryResponseParser();
	private boolean							success		=	false;
	private NamedList						result		=	null;
	private long							rid;
	private long							cid;
	private String 							responseIp;

	public void clear(){
		this.success = false;
		this.result = null;
	}

	public void reset(SolrProtocol.NettyResponse response){
		this.success = response.getSuccess();
		InputStream inputStream = response.getSolrResponse().getResponseBody().getBody().newInput();
		this.result = parser.processResponse(inputStream, "UTF-8");

		this.cid = response.getCid();
		this.rid = response.getRid();
		this.responseIp = response.getResponseIp();
	}


	public boolean isSuccess(){
		return success;
	}

	public long getRid(){
		return rid;
	}

	public long getCid(){
		return cid;
	}

	public String getResponseIp(){
		return responseIp;
	}

	public NamedList getNamedList(){
		return result;
	}
}
