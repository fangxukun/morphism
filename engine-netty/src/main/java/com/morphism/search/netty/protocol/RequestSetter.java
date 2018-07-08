package com.morphism.search.netty.protocol;

import com.morphism.search.netty.common.ByteArrayAccessibleOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * User: xukun.fyp
 * Date: 17/3/28
 * Time: 14:28
 */
public class RequestSetter {
	private SolrProtocol.NettyRequest.Builder			requestBuilder;
	private SolrProtocol.SolrRequest.Builder			solrRequestBuilder;
	private ByteArrayAccessibleOutputStream				baaos;
	private Long 										timeAllowed;

	public RequestSetter(){
		this.requestBuilder = SolrProtocol.NettyRequest.newBuilder();
		this.solrRequestBuilder = SolrProtocol.SolrRequest.newBuilder();
	}

	public void reset(SolrRequest request) throws IOException {
		this.requestBuilder.clear();
		this.solrRequestBuilder.clear();

		if(baaos != null){
			this.baaos.reset();
		}

		if(request.getParams() != null){
			setSolrParam(request.getParams());
			timeAllowed = request.getParams().getLong(CommonParams.TIME_ALLOWED);
		}

		if(request.getContentStreams() != null){
			setContentStreams(request.getContentStreams());
		}

		setPath(request.getPath());
	}

	public RequestSetter setCid(long cid){
		requestBuilder.setCid(cid);
		return this;
	}

	public RequestSetter setRid(long rid){
		requestBuilder.setRid(rid);
		return this;
	}

	public RequestSetter setPath(String path){
		solrRequestBuilder.setPath(path);
		return this;
	}

	public RequestSetter setCollection(String collection){
		solrRequestBuilder.setCollection(collection);
		return this;
	}

	public RequestSetter setRequestIp(String requestIp){
		requestBuilder.setRequestIp(requestIp);
		return this;
	}

	public long getTimeAllowed(long defaultTimeout){
		return timeAllowed != null ? timeAllowed : defaultTimeout;
	}

	private RequestSetter setSolrParam(SolrParams params){
		Iterator<String> names = params.getParameterNamesIterator();

		while(names.hasNext()){
			SolrProtocol.SolrParam.Builder builder = SolrProtocol.SolrParam.newBuilder();
			String name = names.next();
			String[] values = params.getParams(name);

			builder.setKey(name);
			for(String value : values){
				builder.addValues(value);
			}
			solrRequestBuilder.addSolrParam(builder);
		}
		return this;
	}

	private RequestSetter addContentStream(ContentStream contentStream) throws IOException {
		SolrProtocol.ContentStream.Builder builder = SolrProtocol.ContentStream.newBuilder();
		builder.setContentType(contentStream.getContentType());
		builder.setSourceInfo(contentStream.getSourceInfo());
		builder.setSize(contentStream.getSize());

		if(contentStream.getName() != null){
			builder.setName(contentStream.getName());
		}

		if(baaos == null){
			baaos = new ByteArrayAccessibleOutputStream(4192);
		}
		IOUtils.copy(contentStream.getStream(), baaos);
		builder.setStream(baaos.toByteString());
		solrRequestBuilder.addContentStream(builder);
		return this;
	}

	private RequestSetter setContentStreams(Collection<ContentStream> streams) throws IOException{
		for(ContentStream stream : streams){
			addContentStream(stream);
		}
		return this;
	}

	public SolrProtocol.NettyRequest build(){
		requestBuilder.setSolrRequest(solrRequestBuilder);
		return requestBuilder.build();
	}
}
