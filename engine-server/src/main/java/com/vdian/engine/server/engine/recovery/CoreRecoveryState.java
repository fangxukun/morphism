package com.vdian.engine.server.engine.recovery;

import com.vdian.engine.server.engine.data.Indexes;
import com.vdian.engine.server.engine.data.TLogs;
import com.vdian.search.sync.PathSyncClient;
import com.vdian.search.sync.command.list.PathListResponse;
import org.apache.commons.io.FileUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.TransactionLog;
import org.apache.solr.update.UpdateLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * User: xukun.fyp
 * Date: 17/5/19
 * Time: 15:06
 */
public class CoreRecoveryState {
	private final SolrCore			core;
	private final UpdateLog			updateLog;
	private final TargetServer		peerServer;
	private final boolean 			isEmptyCore;					//是空Core，不包含文档。
	private final long 				latestTimeOnStartupLog;			//启动的时候，TLog中最后的一个记录的时间点
	private final boolean 			isTLogHasGap;					//CurrentTime - latestTimeOnLocalLog > oldestTimeOnRemoteLog -> true,will sync by index!


	public CoreRecoveryState(SolrCore core,TargetServer peerServer) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		this.core = core;
		this.peerServer = peerServer;

		this.updateLog = core.getUpdateHandler().getUpdateLog();
		this.isEmptyCore = !Indexes.existDocs(core);

		this.latestTimeOnStartupLog = TLogs.getLatestTimeFromTLog(updateLog);
		this.isTLogHasGap = isTLogHasGap(this.latestTimeOnStartupLog);
	}



	public boolean needSyncIndex(){
		return isEmptyCore || isTLogHasGap;
	}

	/**
	 * 判断本地的TLog和兄弟节点的TLog是否能够接续起来，不能接续则需要同步索引，如果能够接续，同步TLog并回放来接续.
	 * @param latestTimeOnStartupLog			本地TLog中最新时间。
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws IOException
	 */
	private boolean isTLogHasGap(long latestTimeOnStartupLog) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		PathSyncClient client = new PathSyncClient(peerServer.ip,false);
		String localTempPath = Indexes.tempPath(core);
		try{
			String tLogPath = updateLog.getLogDir();

			PathListResponse pathList = client.listPath(tLogPath);
			Optional<PathListResponse.TPath> oldestPath = pathList.getTPaths().stream().sorted().findFirst();

			if(oldestPath.isPresent()){

				client.syncPath(oldestPath.get().pathName,localTempPath);

				File oldestLogFile = Paths.get(localTempPath, oldestPath.get().getFileName()).toFile();
				TransactionLog tLog = updateLog.newTransactionLog(oldestLogFile, null, true);
				long oldestTimeOnLog = TLogs.timeRangeOfTLog(tLog).getLeft();		//兄弟节点TLog最老的时间

				if(oldestTimeOnLog > latestTimeOnStartupLog){
					return true;
				}
			}

			return false;
		}finally {
			if(client != null){
				client.shutdown();
			}
			FileUtils.deleteDirectory(new File(localTempPath));
		}
	}
}
