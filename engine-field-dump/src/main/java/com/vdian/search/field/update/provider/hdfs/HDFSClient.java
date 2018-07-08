package com.vdian.search.field.update.provider.hdfs;


import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.net.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 10:22
 */
public class HDFSClient {
	private static final Logger				LOGGER		= 	LoggerFactory.getLogger(HDFSClient.class);
	private static final String 			CONF_FILE	=	"configuration.properties";
	private static final String 			PROXY_FILE	=	"proxy.properties";
	private final Configuration				conf;
	private final FileSystem 				fileSystem;


	public HDFSClient(String checkPath) throws IOException {
		this(new Configuration(),checkPath,false);
	}
	public HDFSClient(String checkPath,boolean proxy) throws IOException {
		this(new Configuration(),checkPath,proxy);
	}

	public HDFSClient(Configuration conf,String checkPath,boolean proxy) throws IOException {
		this.conf = conf;
		if(proxy){
			initFromProperties(conf,PROXY_FILE);
		}
		initFromProperties(conf,CONF_FILE);

		FileSystem fs;
		try{
			fs = FileSystem.get(conf);
			fs.listStatus(new Path(checkPath));
		}catch (ConnectTimeoutException e){
			initFromProperties(conf,PROXY_FILE);
			fs = FileSystem.newInstance(conf);
			fs.listStatus(new Path(checkPath));
		}
		this.fileSystem = fs;
	}

	public void copyToLocal(String remotePath,String localPath) throws IOException {
		String resolvedPath = PathResolver.resolve(remotePath, fileSystem);
		LOGGER.warn("[HFDSClient] copyToLocal,remotePath:{},resolvedPath:{},localPath:{}", remotePath, resolvedPath, localPath);

		if(Files.exists(Paths.get(localPath))){
			LOGGER.warn("[HDFSClient] delete local path:{}", localPath);
			FileUtils.deleteQuietly(new File(localPath));
		}
		this.fileSystem.copyToLocalFile(new Path(resolvedPath), new Path(localPath));

		long localSize = -1;
		if(Files.isDirectory(Paths.get(localPath))){
			localSize = FileUtils.sizeOfDirectory(new File(localPath));
		}else{
			localSize = FileUtils.sizeOf(new File(localPath));
		}

		LOGGER.warn("[HFDSClient] copyToLocal complete! copy size:{} MB", Double.valueOf(localSize)/(1024*1024));
	}


	private void initFromProperties(Configuration conf,String resourceName) throws IOException {
		Properties prop = new Properties();
		prop.load(Resources.getResource(resourceName).openStream());

		for(String name : prop.stringPropertyNames()){
			conf.set(name,prop.getProperty(name));
		}
	}
}
