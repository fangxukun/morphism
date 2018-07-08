package com.vdian.engine.server.update.processor;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/25
 * Time: 15:59
 */
public class VersionConstraintProcessorFactory extends UpdateRequestProcessorFactory {
	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
		return null;
	}


	public class VersionConstraintProcessor extends UpdateRequestProcessor{

		public VersionConstraintProcessor(UpdateRequestProcessor next) {
			super(next);
		}

		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			final SolrInputDocument newDoc = cmd.getSolrInputDocument();

		}
	}
}
