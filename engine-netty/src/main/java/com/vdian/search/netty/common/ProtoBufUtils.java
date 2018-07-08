package com.vdian.search.netty.common;

import com.vdian.search.netty.protocol.SolrProtocol;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/24
 * Time: 18:20
 */
public class ProtoBufUtils {

//	public static void extractSolrParams(SolrProtocol.SolrRequest request, ModifiableSolrParams internalParam){
//		internalParam.clear();
//		List<SolrProtocol.SolrParam> paramList = request.getSolrParamList();
//		for(SolrProtocol.SolrParam param : paramList){
//			List<String> values = param.getValuesList();
//			if(values == null){
//				internalParam.add(param.getKey(),null);
//			}else{
//				internalParam.add(param.getKey(),values.toArray(new String[values.size()]));
//			}
//		}
//	}

	public static void setContentType(SolrProtocol.SolrResponse.Builder builder,String contentType){
		builder.getResponseBodyBuilder().setContentType(contentType);
	}

	public static void setException(SolrProtocol.SolrResponse.Builder builder,int code,Exception e){
		SolrProtocol.ExceptionBody.Builder exceptionBuilder = SolrProtocol.ExceptionBody.newBuilder();
		exceptionBuilder.setCode(code);
		exceptionBuilder.setMessage(ExceptionUtils.getFullStackTrace(e));
		builder.addExceptionBody(exceptionBuilder);
	}


}
