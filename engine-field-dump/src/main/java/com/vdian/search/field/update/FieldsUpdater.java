package com.vdian.search.field.update;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vdian.search.field.dump.DumpLayout;
import com.vdian.search.field.update.lucene.LuceneUtils;
import com.vdian.search.field.update.provider.DataProvider;
import com.vdian.search.field.update.provider.DataProviders;
import com.vdian.search.field.update.provider.Record;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.RefCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/4/13
 * Time: 11:17
 */
public class FieldsUpdater {
	private static final Logger			LOGGER						=	LoggerFactory.getLogger(FieldsUpdater.class);
	private static final int 			THREAD_SIZE					=	4;
	private static final int			TOLERANCE_FAILED_COUNT		=	1000/THREAD_SIZE;
	private static final long 			TIMEOUT						=	20;

	private final DumpLayout			layout;
	private final SolrCore				core;

	private ResultContext				resultContext;
	private UpdateContext				updateContext;
	private DataProvider				provider;

	private ArrayBlockingQueue<Record>	blockingQueue;
	private ExecutorService				updateService;
	private boolean 					isCancel;


	public FieldsUpdater(DumpLayout layout,SolrCore core){
		this.core = core;
		this.layout = layout;
	}

	public void init() throws UpdateException, IOException {
		this.isCancel = false;
		this.updateContext = new UpdateContext(layout,core.getLatestSchema(),LuceneUtils.getFieldInfo(core));
		this.resultContext = new ResultContext(TOLERANCE_FAILED_COUNT);
		this.updateContext.checkFields();

		this.provider = DataProviders.newProvider(updateContext);
		this.provider.init();
		this.resultContext.markPullComplete();

		this.blockingQueue = new ArrayBlockingQueue<Record>(100);

		ThreadFactory factory = new ThreadFactoryBuilder()
				.setNameFormat("docValueUpdate")
				.setDaemon(true)
				.build();
		this.updateService = Executors.newFixedThreadPool(THREAD_SIZE,factory);


	}


	public String updateDocValues() throws IOException, UpdateException, InterruptedException {
		RefCounted<IndexWriter> iw = null;
		try{
			iw = core.getSolrCoreState().getIndexWriter(core);


			for(int i=0;i<THREAD_SIZE;i++){
				this.updateService.execute(new Consumer(blockingQueue,iw.get(),resultContext));
			}

			offerRecordToQueue();
			this.updateService.shutdown();
			boolean complete = this.updateService.awaitTermination(1,TimeUnit.HOURS);

			if(complete  == false){
				throw new UpdateException("update thread execute timeout,timeout: 1h");
			}

			resultContext.markUpdateComplete();
			commit(iw.get());
			return resultContext.toJson();
		}catch (Exception e){
			e.printStackTrace();
			return e.getMessage();
		}finally {
			if(iw != null){
				iw.decref();
			}
		}
	}

	private void commit(IndexWriter indexWriter) throws IOException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		LOGGER.warn("start to flush and commit!");
		indexWriter.flush();
		indexWriter.commit();
		LOGGER.warn("commit complete! cost:" + stopwatch.elapsed(TimeUnit.SECONDS) + "s");
	}

	public String stop(){
		this.isCancel = true;
		return "cancel success!";
	}

	private void offerRecordToQueue() throws UpdateException {
		int successCount = 0,failedCount = 0;
		for(Record record : provider.readRecords()){
			if(isCancel == false){
				try{
					this.blockingQueue.offer(record,TIMEOUT, TimeUnit.SECONDS);
					successCount++;
				}catch (InterruptedException e){
					failedCount++;
					throw new UpdateException(String.format("thread is interrupt,dump is canceled,success:%s,failed:%s",successCount,failedCount),e);
				}
			}else{
				LOGGER.warn("dump is canceled,offerRecord stoped,success:{},failed:{}",successCount,failedCount);
			}
		}

		LOGGER.warn("offer record complete success:{},failed:{}",successCount,failedCount);

	}



	public static class Consumer implements Runnable{
		private static final Logger					LOGGER			= LoggerFactory.getLogger(Consumer.class);
		private final BlockingQueue<Record> 		queue;
		private final IndexWriter					indexWriter;
		private final ResultContext						context;

		public Consumer(BlockingQueue<Record> queue,IndexWriter indexWriter,ResultContext context){
			this.queue = queue;
			this.indexWriter = indexWriter;
			this.context = context;
		}

		@Override
		public void run() {
			Record record = null;
			try{
				int count = 0;
				while((record = queue.poll(20,TimeUnit.SECONDS)) != null){
					Term term = record.getKeyTerm();

					try{
						long[] numericValues = record.getNumericValues();
						String[] numericFields = record.getNumericFields();
						for(int i=0;i<numericFields.length;i++){
							indexWriter.updateNumericDocValue(term,numericFields[i],numericValues[i]);
						}

						BytesRef[] binaryValues = record.getBinaryValues();
						String[] binaryFields = record.getBinaryFields();
						for(int i=0;i<binaryFields.length;i++){
							indexWriter.updateBinaryDocValue(term,binaryFields[i],binaryValues[i]);
						}

						context.reportSuccess();
						count++;
					}catch (IOException e){
						context.reportFailed(e);
					}
				}
			}catch (InterruptedException e){
				context.interrupted();
			}catch (UpdateException e){
				LOGGER.error(String.format("Thread:%s is ended",Thread.currentThread().getName()),e);
			}
		}
	}


}
