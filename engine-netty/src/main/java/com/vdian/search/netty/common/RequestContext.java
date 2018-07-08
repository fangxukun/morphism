package com.vdian.search.netty.common;

import com.vdian.search.netty.protocol.RequestSetter;
import com.vdian.search.netty.protocol.ResponseGetter;
import com.vdian.search.netty.protocol.SolrProtocol;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * User: xukun.fyp
 * Date: 16/12/14
 * Time: 11:47
 * 管理request/responseContent，
 */
public class RequestContext {
	private long 									rid;
	private long 									cid;

	private final RequestSetter						requestSetter;
	private final ResponseGetter					responseGetter;
	private InetSocketAddress						remoteAddress;
	private boolean 								complete;

	private long 									start;
	private long 									cost;
	private long 									timeout;


	public RequestContext(){
		this.responseGetter = new ResponseGetter();
		this.requestSetter = new RequestSetter();
		complete = false;
	}


	public void reset(SolrRequest request,InetSocketAddress remoteAddress,String collection) throws IOException {
		this.requestSetter.reset(request);
		this.requestSetter.setCollection(collection);
		this.remoteAddress = remoteAddress;
		reset();
	}

	private void reset(){
		this.responseGetter.clear();
		this.start = System.currentTimeMillis();
		this.cost = -1;
		this.rid = -1;
		this.complete = false;
	}

	public void setSession(long requestId,long connectId){
		this.rid = requestId;
		this.cid = connectId;
		this.requestSetter.setCid(connectId).setRid(rid);
	}

	public void completeRpc(SolrProtocol.NettyResponse response){
		this.responseGetter.reset(response);
		this.complete = true;
		this.cost = System.currentTimeMillis() - this.start;
	}

	public UpdateResponse getUpdateResponse() throws NoResponseException{
		if(isSuccess()){
			NamedList result = responseGetter.getNamedList();
			UpdateResponse response = new UpdateResponse();
			response.setResponse(result);
			return response;
		}else{
			throw new NoResponseException();
		}
	}

	public QueryResponse getQueryResponse() throws NoResponseException{
		if(isSuccess()){
			NamedList result = responseGetter.getNamedList();
			QueryResponse response = new QueryResponse();
			response.setResponse(result);
			return response;
		}else{
			throw new NoResponseException();
		}
	}

	public NamedList getNamedList(){
		return responseGetter.getNamedList();
	}

	public SolrProtocol.NettyRequest buildRequest(){
		requestSetter.setRequestIp(NetworkUtils.getLocalHostAddress());
		return requestSetter.build();
	}
	public void setTimeout(long timeoutMs){
		timeout = timeoutMs;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public long getRid() {
		return rid;
	}

	public long getCid(){
		return cid;
	}

	public long getCost() {
		return cost;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isSuccess(){
		return complete && responseGetter.isSuccess();
	}

	public String visibleResponse(){
		StringBuilder builder = new StringBuilder();
		builder.append("rid:").append(rid);
		builder.append("cid:").append(responseGetter.getCid());
		builder.append("serverIp:").append(responseGetter.getResponseIp());
		NamedList response = responseGetter.getNamedList();
		builder.append("namedList:").append(response);
		return builder.toString();
	}

	public long getRequestTimeout(long defaultTimeout){
		if(timeout > 0){
			return timeout;
		}else{
			return this.requestSetter.getTimeAllowed(defaultTimeout);
		}

	}
}
