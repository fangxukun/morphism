package com.morphism.engine.server.engine.recovery;

import com.morphism.engine.server.engine.data.Indexes;
import com.morphism.engine.server.engine.data.TLogs;
import com.morphism.engine.server.cloud.VModelCloud;
import com.morphism.search.sync.PathSyncClient;
import com.morphism.search.sync.command.list.PathListResponse;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.RecoveryUpdateLog;
import org.apache.solr.update.TransactionLog;
import org.apache.solr.update.UpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * User: xukun.fyp
 * Date: 17/5/4
 * Time: 11:05
 */
public class PeerRecovery {
	private static final Logger			LOGGER			= LoggerFactory.getLogger(PeerRecovery.class);
	private final SolrCore				core;
	private final RecoveryUpdateLog 	ulog;
	private final VModelCloud			cloud;

	private final String 				tempPath;
	private final String 				coreDataPath;

	private final TargetServer			peerServer;

	public PeerRecovery(VModelCloud cloud,SolrCore core){
		this.core = core;
		this.cloud = cloud;


		UpdateLog ulog = core.getUpdateHandler().getUpdateLog();
		if(ulog instanceof RecoveryUpdateLog){
			this.ulog = (RecoveryUpdateLog)ulog;
		}else{
			throw new RuntimeException("must used RecoveryUpdateLog");
		}

		this.tempPath = Indexes.tempPath(core);
		this.coreDataPath = core.getDataDir();

		Optional<TargetServer> serverOptional = cloud.peerServer(core.getName());

		this.peerServer = serverOptional.isPresent() ? serverOptional.get() : null;
	}


	public boolean recoveryOnStartup() throws Exception {
		if(peerServer == null){
			return false;
		}

		PathSyncClient client = new PathSyncClient(peerServer.ip,false);
		try{
			CoreRecoveryState state = new CoreRecoveryState(core,peerServer);
			boolean needSyncIndex = state.needSyncIndex();

			//1.如果本地Index数据为空，则同步Index
			if(needSyncIndex){
				TLogs.commitOnServer(peerServer.baseUrlWithCore);
				syncIndex(client);
			}

			//在这个里面存在一种可能性，回放的TLog中存在新增记录，并且在bufferUpdates期间有这个记录的部分更新记录，此时会导致这个更新记录的字段不全
			this.ulog.bufferUpdates();
			this.cloud.onlineWriter();

			//1.如果本地Index数据为空，则同步Index并同步TLog
			TLogs.commitOnServer(peerServer.baseUrlWithCore);
			syncTLog(client);

		}finally {
			ulog.applyBufferedUpdates();
			client.shutdown();
		}
		return true;
	}

	/**
	 * 从兄弟节点直接同步index到本地，并reload Core
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void syncIndex(PathSyncClient client) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		String indexDirName = Indexes.newIndexDirName(Paths.get(coreDataPath));
		Path localIndexPath = Paths.get(core.getDataDir(),indexDirName);
		Path remoteIndexPath = remoteCurrentIndexPath(client);

		client.syncPath(remoteIndexPath,localIndexPath);

		Indexes.switchIndex(core, indexDirName);
		LOGGER.warn("sync index from {}:{} to localhost:{}",peerServer.ip,remoteIndexPath,localIndexPath);
	}

	/**
	 * 从兄弟节点同步TLog到本地，并Replay 同步过来的TLog
	 * @throws IOException
	 * @throws SolrServerException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private void syncTLog(PathSyncClient client) throws IOException, SolrServerException, InterruptedException, ExecutionException, TimeoutException {
		try{
			List<PathListResponse.TPath> tLogs = client.listPath(ulog.getLogDir()).getTPaths();
			Collections.sort(tLogs, (PathListResponse.TPath o1,PathListResponse.TPath o2) -> o2.pathName.compareTo(o1.pathName));

			long startTime = TLogs.getLatestTimeFromTLog(ulog);

			for(PathListResponse.TPath path : tLogs){
				if(path.isDirectory){
					continue;
				}

				client.syncPath(path.pathName,tempPath);
				Path tLogPath = Paths.get(tempPath,path.getFileName());

				try(TransactionLog tLog = ulog.newTransactionLog(tLogPath.toFile(),null,true,false)){
					if(TLogs.hasRecordAfterTime(tLog, startTime) == false){
						break;
					}
					LOGGER.warn("sync tlog from {}:{} to localhost:{}",peerServer.ip,path.pathName,tempPath);
				}
			}

			Files.list(Paths.get(tempPath)).sorted().forEach(ulog::addOuterLogFile);
			ulog.recoverFromLog(startTime);
		}finally {
			FileUtils.deleteDirectory(new File(tempPath));
		}
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private Path remoteCurrentIndexPath(PathSyncClient client) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		String remoteIndexPropertiesPath = Indexes.indexPropertiesPath(coreDataPath);
		if(client.existPath(Paths.get(remoteIndexPropertiesPath))){
			client.syncPath(remoteIndexPropertiesPath,tempPath);

			Path localIndexProp = Paths.get(Indexes.indexPropertiesPath(tempPath));
			String indexPath = Indexes.readIndexFromProperties(localIndexProp);
			return Paths.get(coreDataPath,indexPath);
		}else{
			return Paths.get(coreDataPath,"index");
		}



	}
}
