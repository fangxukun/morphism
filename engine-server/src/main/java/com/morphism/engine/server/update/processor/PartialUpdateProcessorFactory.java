package com.morphism.engine.server.update.processor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.UpdateCommand;
import org.apache.solr.update.UpdateLog;
import org.apache.solr.update.processor.AtomicUpdateDocumentMerger;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: xukun.fyp
 * Date: 17/5/24
 * Time: 16:26
 * 后续考虑直接使用SolrCloud方案，目前这么做主要是兼容目前微店的现有体系架构
 *
 * 此Processor主要处理部分更新相关的逻辑，对于本系统可以替换掉DistributedUpdateProcessor
 */
public class PartialUpdateProcessorFactory extends UpdateRequestProcessorFactory{
	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
		return new PartialUpdateProcessor(req,rsp,next);
	}

	public class PartialUpdateProcessor extends UpdateRequestProcessor{
		public static final String			UPDATE_FIELDS			=	"i_u_field";
		public static final String 			PARTIAL_MOCK_FIELDS		=	"i_p_m_field";


		private final SolrQueryRequest				request;
		private final SolrQueryResponse				response;
		private final UpdateLog						updateLog;
		private final AtomicUpdateDocumentMerger	merger;
		private final SolrCore						core;

		public PartialUpdateProcessor(SolrQueryRequest req, SolrQueryResponse rsp,UpdateRequestProcessor next) {
			super(next);

			this.request = req;
			this.response = rsp;
			this.core = req.getCore();
			this.updateLog = req.getCore().getUpdateHandler().getUpdateLog();
			this.merger = new AtomicUpdateDocumentMerger(req);

			Preconditions.checkState(updateLog != null,"updateLog must configured!");
		}


		/**
		 *
		 * 1.对于部分更新的，做完全量回放TLog时不能覆盖全量中更新的一些字段内容。
		 * @param cmd
		 * @throws IOException
		 */
		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			boolean isPartialUpdate = AtomicUpdateDocumentMerger.isAtomicUpdate(cmd);
			boolean isReplay = (cmd.getFlags() & UpdateCommand.REPLAY) != 0;
			SolrInputDocument document = cmd.getSolrInputDocument();

			//对于正常部分更新请求，在doc中添加上部分更新的字段信息。
			if(isPartialUpdate){
				if(!document.containsKey(UPDATE_FIELDS)){
					String updateFields = document
							.values()
							.stream()
							.map(field -> field.getName())
							.collect(Collectors.joining(","));
					document.setField(UPDATE_FIELDS,updateFields);
				}
			}

			if(isReplay){
				String updateFieldStr = (String)document.getFieldValue(UPDATE_FIELDS);
				if(updateFieldStr != null){
					Set<String> updateFields = Sets.newHashSet(updateFieldStr.split(","));

					Iterator<SolrInputField> fields = document.iterator();
					while(fields.hasNext()){
						SolrInputField field = fields.next();
						if(!updateFields.contains(field.getName())){
							fields.remove();
						}
					}

					//将Replay的请求Mock为包含部分更新的逻辑，remove掉的字段后续会和引擎里面的老doc进行merge
					document.setField(PARTIAL_MOCK_FIELDS,new HashMap<>());
				}
			}

			super.processAdd(cmd);
		}

	}
}
