package com.morphism.search.netty.server;

import com.google.common.base.Preconditions;
import com.morphism.search.netty.common.ByteArrayAccessibleOutputStream;
import com.morphism.search.netty.protocol.RequestGetter;
import com.morphism.search.netty.protocol.ResponseSetter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.QueryResponseWriterUtil;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.SolrRequestParsers;

import java.io.IOException;
import java.util.Collection;

/**
 * User: xukun.fyp
 * Date: 17/3/24
 * Time: 19:18
 * 封装屏蔽来之Solr相关的细节。
 */
public class SolrCoreDelegate {
	public static final int 						INTERNAL_ERROR_CODE		=	500;

	private final CoreContainer						container;
	private final SolrRequestParsers 				defaultParser;
	private final ByteArrayAccessibleOutputStream 	output;

	public SolrCoreDelegate(CoreContainer container){
		this.container = container;
		this.defaultParser = SolrRequestParsers.DEFAULT;
		this.output = new ByteArrayAccessibleOutputStream(8192);
	}


	public void request(RequestGetter requestGetter,ResponseSetter responseSetter) throws Exception{
		String wt = requestGetter.getWriteType();

		SolrCore core = container.getCore(requestGetter.getCollection());
		Preconditions.checkNotNull(core, "collection:%s can not find!", requestGetter.getCollection());

		SolrRequestHandler handler = core.getRequestHandler(requestGetter.getPath());
		Preconditions.checkNotNull(handler, "request path:%s can not find handler", requestGetter.getPath());

		SolrParams solrParams = requestGetter.getSolrParams();
		Collection contentStreamList = requestGetter.getContentStreamList();
		SolrQueryRequest internalRequest = defaultParser.buildRequestFrom(core,solrParams,contentStreamList);
		SolrQueryResponse internalResponse = new SolrQueryResponse();

		try{
			core.execute(handler, internalRequest, internalResponse);
		}catch (Throwable r){
			r.printStackTrace();
		}

		QueryResponseWriter writer = SolrCore.DEFAULT_RESPONSE_WRITERS.get(wt);

		this.writeResponse(
				internalRequest,
				internalResponse,
				writer,
				responseSetter);
	}

	private void writeResponse(SolrQueryRequest request,
							   SolrQueryResponse response,
							   QueryResponseWriter writer,
							   ResponseSetter responseSetter) throws IOException{
		String contentType = writer.getContentType(request,response);
		if(contentType != null){
			responseSetter.setContentType(contentType);
		}

		if(response.getException() != null){
			responseSetter.setSolrException(response.getException());
		}

		output.reset();
		QueryResponseWriterUtil.writeQueryResponse(output,writer,request,response,contentType);

		responseSetter.setResponse(output.getBuffer(),0,output.size());
	}

}
