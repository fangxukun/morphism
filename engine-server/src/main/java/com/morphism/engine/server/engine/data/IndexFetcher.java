package com.morphism.engine.server.engine.data;

import com.morphism.search.sync.PathSyncClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/5/26
 * Time: 10:15
 */
public class IndexFetcher {
	private static final Logger		LOGGER			= LoggerFactory.getLogger(IndexFetcher.class);
	public final SolrCore			core;
	public final String 			dataDir;


	public IndexFetcher(SolrCore core){
		this.core = core;
		this.dataDir = core.getDataDir();
	}

	/**
	 * 从远程服务器上拉取索引到本地目录。
	 * @param serverIp			远程服务器ip,服务器必须部署PathSyncServer.
	 * @param remotePathStr		远程服务器上需要同步到本地的数据路径。
	 * @param limitMB			速度限制，(1~100MB)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String fetchFromServer(String serverIp,String remotePathStr,int limitMB) throws IOException, InterruptedException {
		String newIndexDirName = Indexes.newIndexDirName(Paths.get(dataDir));

		Path localPath = Paths.get(dataDir, newIndexDirName);
		Path remotePath = Paths.get(remotePathStr);
		LOGGER.warn("from {}:{} to local:{} speed {}", serverIp, remotePathStr, localPath, limitMB);

		PathSyncClient client = new PathSyncClient(serverIp,limitMB,true);
		client.syncPath(remotePath,localPath);

		return newIndexDirName;
	}

	/**
	 * 从HDFS上拷贝数据到本地索引目录
	 * @param conf
	 * @param remotePathStr
	 * @throws IOException
	 */
	public String fetchFromHDFS(Configuration conf,String remotePathStr) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		String newIndexDirName = Indexes.newIndexDirName(Paths.get(dataDir));

		org.apache.hadoop.fs.Path srcPath = new org.apache.hadoop.fs.Path(remotePathStr);
		org.apache.hadoop.fs.Path dstPath = new org.apache.hadoop.fs.Path(dataDir,newIndexDirName);
		LOGGER.warn("from hdfs:{} to local:{}",srcPath,dstPath);

		fs.copyToLocalFile(srcPath,dstPath);
		return newIndexDirName;
	}
}
