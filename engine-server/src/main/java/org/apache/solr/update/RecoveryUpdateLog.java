package org.apache.solr.update;

import com.vdian.engine.server.engine.data.TLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * User: xukun.fyp
 * Date: 17/5/4
 * Time: 10:11
 * 此UpdateLog会替换UpdateLog
 */
public class RecoveryUpdateLog extends UpdateLog {
	private static final Logger				LOGGER		= LoggerFactory.getLogger(RecoveryUpdateLog.class);

	public RecoveryUpdateLog(){
	}

	/**
	 * 从一个指定的时间开始恢复后续所有的TLog, 用于全量更新后，回放中间的Gap.
	 * @param startTimeMillis
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void recoverFromLog(long startTimeMillis) throws ExecutionException, InterruptedException, IOException {
		List<TransactionLog> logs = getLogsAfterTime(startTimeMillis);

		if(logs.isEmpty()){
			LOGGER.warn("recovery logs is empty,startTimeMillis:" + startTimeMillis);
			return;
		}

		for(TransactionLog log : logs){
			log.try_incref();
		}

		ExecutorCompletionService<RecoveryInfo> cs = new ExecutorCompletionService<>(recoveryExecutor);
		LogReplayer replayer = new LogReplayer(logs,true);

		Future<RecoveryInfo> future = cs.submit(replayer, recoveryInfo);
		RecoveryInfo recoveryInfo = future.get();
		LOGGER.warn("recovery from time:{} complete,recoveryInfo:{}",startTimeMillis,recoveryInfo);
	}


	public void addOuterLogFile(Path srcPath){
		try{
			String destLogName = String.format(Locale.ROOT, LOG_FILENAME_PATTERN, TLOG_NAME, id++);
			Path destPath = Paths.get(tlogDir.getAbsolutePath(),destLogName);

			while(Files.exists(destPath)){
				destLogName = String.format(Locale.ROOT, LOG_FILENAME_PATTERN, TLOG_NAME, id++);
				destPath = Paths.get(tlogDir.getAbsolutePath(),destLogName);
			}

			Files.move(srcPath,destPath);
			addOldLog(newTransactionLog(destPath.toFile(),globalStrings,true),true);
		}catch (IOException e){
			throw new RuntimeException(e);
		}
	}



	public TransactionLog newTransactionLog(File tlogFile, Collection<String> globalStrings, boolean openExisting,boolean deleteOnClose) {
		TransactionLog transactionLog = newTransactionLog(tlogFile,globalStrings,openExisting);
		transactionLog.deleteOnClose = deleteOnClose;
		return transactionLog;
	}

	public List<TransactionLog> getLogsAfterTime(long startTimeMillis) throws IOException {
		LinkedList<TransactionLog> result = new LinkedList<>();

		for(TransactionLog log : logs){
			long maxTime = TLogs.timeRangeOfTLog(log).getRight();
			if(maxTime > startTimeMillis){
				result.addFirst(log);			//老的在最前面
			}
		}
		return result;
	}
}
