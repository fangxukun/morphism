package com.vdian.engine.server.engine.data;

import com.vdian.search.sync.PathSyncClient;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.RecoveryUpdateLog;
import org.apache.solr.update.TransactionLog;
import org.apache.solr.update.UpdateLog;
import org.apache.solr.update.processor.DistributedUpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/17
 * Time: 14:04
 */
public class TLogs {
	private static final Logger LOGGER = LoggerFactory.getLogger(TLogs.class);

	public static List<TransactionLog> getLogsAfterTime(UpdateLog updateLog, long startTimeMillis) throws IOException {
		if (updateLog instanceof RecoveryUpdateLog) {
			return ((RecoveryUpdateLog) updateLog).getLogsAfterTime(startTimeMillis);
		} else {
			throw new UnsupportedOperationException();
		}
	}


	/**
	 * 在指定的引擎上提交Commit.
	 *
	 * @param targetServerUrl
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static void commitOnServer(String targetServerUrl) throws IOException, SolrServerException {
		try (HttpSolrClient client = new HttpSolrClient.Builder(targetServerUrl).build()) {
			client.setConnectionTimeout(30000);
			UpdateRequest request = new UpdateRequest();
			request.setParams(new ModifiableSolrParams());

			request.getParams().set(DistributedUpdateProcessor.COMMIT_END_POINT, true);
			request.getParams().set(UpdateParams.OPEN_SEARCHER, false);
			request.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, true).process(client);
		}
	}


	/**
	 * 判断指定的TLog是否包含在startTime之后的记录。一般用于判断是否需要Replay此Log。
	 *
	 * @param tlog
	 * @param startTime
	 * @return
	 * @throws IOException
	 */
	public static boolean hasRecordAfterTime(TransactionLog tlog, long startTime) throws IOException {
		long maxTime = timeRangeOfTLog(tlog).getRight();
		if (maxTime > 0 && maxTime <= startTime) {
			return false;
		}
		return true;
	}


	/**
	 * 获取本地TLog重启后最近的更新时间点，一般需要启动后没有新写入数据的时候调用。
	 *
	 * @return 如果没有TLog返回 0
	 */
	public static long getLatestTimeFromTLog(UpdateLog ulog) {
		long startTime = 0;
		List<Long> startingVersions = ulog.getStartingVersions();
		if (startingVersions != null && !startingVersions.isEmpty()) {
			long startVersion = startingVersions.get(0);
			startTime = timeFromVersion(startVersion);
		} else {
			LOGGER.warn("tlog is empty? startTime will set to 0,and may sync and replay all tlog in peer node!");
		}
		return startTime;
	}



	public static Pair<Long, Long> timeRangeOfTLog(TransactionLog log) throws IOException {
		TransactionLog.ReverseReader reader = log.getReverseReader();
		long min = -1, max = -1;
		try {
			List entry = null;
			while ((entry = (List) reader.next()) != null) {
				long version = (Long) entry.get(UpdateLog.VERSION_IDX);
				long time = timeFromVersion(version);

				min = min == -1 ? time : Math.min(min, time);
				max = max == -1 ? time : Math.max(max, time);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return Pair.of(min,max);
	}



	public static Path tempDirectory(SolrCore core) {
		return Paths.get(core.getDataDir(), ".temp");
	}

	/**
	 * 从Version计算Time
	 *
	 * @param version
	 * @return
	 */
	public static long timeFromVersion(long version) {
		return version >> 20;
	}
}
