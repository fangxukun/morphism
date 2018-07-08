package com.vdian.search.ann.lucene.query;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 15:28
 */
public class AnnQParserPlugin extends QParserPlugin{
	private static final Logger		LOGGER				= LoggerFactory.getLogger(AnnQParserPlugin.class);
	public static final String 		FIELD				=	"field";
	public static final String 		VECTOR				=	"vector";
	public static final String 		SEARCH_NUM			=	"searchNum";
	public static final int 		DEFAULT_SEARCH_NUM	=	10000;

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new AnnQParser(qstr,localParams,params,req);
	}

	@Override
	public void init(NamedList args) {

	}


	public class AnnQParser extends QParser{

		public AnnQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req){
			super(qstr,localParams,params,req);
		}

		@Override
		public Query parse() throws SyntaxError {
			String field = localParams.get(FIELD);
			if(field == null){
				field = getParam(FIELD);
				if(field == null){
					throw new SolrException(SolrException.ErrorCode.BAD_REQUEST," missing filed for ann search!");
				}
			}

			String vector = getParam(VECTOR);
			if(vector == null){
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST," missing vector for ann search!");
			}

			String[] vs = vector.split(",");
			float[] queryVector = new float[vs.length];
			for(int i=0;i<vs.length;i++){
				queryVector[i] = Float.parseFloat(vs[i]);
			}

			int searchNum = DEFAULT_SEARCH_NUM;
			String searchNumStr = localParams.get(SEARCH_NUM);
			if(searchNumStr == null){
				searchNumStr = getParam(SEARCH_NUM);
			}
			if(searchNumStr != null){
				searchNum = Integer.parseInt(searchNumStr);
			}

			LOGGER.warn("field:{},vsSize:{},searchNum:{}",field,queryVector.length,searchNum);

			return new AnnVectorQuery(field,searchNum,queryVector);
		}
	}
}
