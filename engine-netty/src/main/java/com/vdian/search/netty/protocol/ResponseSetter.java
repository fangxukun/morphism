package com.vdian.search.netty.protocol;

import com.google.protobuf.BoundedByteAccessibleString;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * User: xukun.fyp
 * Date: 17/3/27
 * Time: 14:49
 */
public class ResponseSetter {
	private final SolrProtocol.NettyResponse.Builder	responseBuilder;
	private final SolrProtocol.SolrResponse.Builder		solrResponseBuilder;
	private final SolrProtocol.ExceptionBody.Builder	exceptionBuilder;


	public ResponseSetter(){
		this.responseBuilder = SolrProtocol.NettyResponse.newBuilder();
		this.solrResponseBuilder = SolrProtocol.SolrResponse.newBuilder();
		this.exceptionBuilder = SolrProtocol.ExceptionBody.newBuilder();
	}


	public void reset(RequestGetter requestGetter){
		responseBuilder.clear();
		solrResponseBuilder.clear();

		responseBuilder.setCid(requestGetter.getCid());
		responseBuilder.setRid(requestGetter.getRid());
		responseBuilder.setResponseIp(NetworkUtils.getLocalHostAddress());
	}

	public void setException(Exception e){
		exceptionBuilder.setCode(500);
		exceptionBuilder.setMessage(ExceptionUtils.getFullStackTrace(e));
		solrResponseBuilder.addExceptionBody(exceptionBuilder);
		responseBuilder.setSuccess(false);
	}

	public void setSolrException(Exception e){
		SolrProtocol.ExceptionBody.Builder exceptionBuilder = SolrProtocol.ExceptionBody.newBuilder();
		exceptionBuilder.setCode(499);
		exceptionBuilder.setMessage(ExceptionUtils.getFullStackTrace(e));
		solrResponseBuilder.addExceptionBody(exceptionBuilder);
		responseBuilder.setSuccess(false);
	}


	public void setContentType(String contentType){
		solrResponseBuilder.getResponseBodyBuilder().setContentType(contentType);
	}


	public void setResponse(byte[] buffer,int offset,int length){
		BoundedByteAccessibleString byteString = new BoundedByteAccessibleString(buffer,offset,length);
		solrResponseBuilder.getResponseBodyBuilder().setBody(byteString);
		responseBuilder.setSuccess(true);
	}


	public SolrProtocol.NettyResponse build(){
		responseBuilder.setSolrResponse(solrResponseBuilder);
		return responseBuilder.build();
	}
}
