package com.vdian.search.field.update.provider.hdfs;

import com.vdian.search.field.update.UpdateContext;
import com.vdian.search.field.update.UpdateException;
import com.vdian.search.field.update.provider.DataProvider;
import com.vdian.search.field.update.provider.Record;
import com.vdian.search.field.update.provider.text.TextDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/4/14
 * Time: 11:38
 */
public class HDFSTextDataProvider implements DataProvider {
	private static final Logger		LOGGER			= LoggerFactory.getLogger(HDFSTextDataProvider.class);
	private final UpdateContext		context;

	private TextDataProvider		delegate;
	private HDFSClient				client;


	public HDFSTextDataProvider(UpdateContext context){
		this.context = context;
	}

	public void init() throws IOException, UpdateException {
		this.client = new HDFSClient(context.layout.remotePath,context.layout.proxy);
		this.client.copyToLocal(context.layout.remotePath,context.layout.localPath);

		this.delegate = new TextDataProvider(context);
		this.delegate.init();

		LOGGER.warn(
				"copy from hdfs and init complete,hdfs:{},local:{},fileCount:{},fileLength:{}",
				context.layout.remotePath,
				context.layout.localPath,
				delegate.fileCount(),
				delegate.fileLength());
	}

	@Override
	public Iterable<Record> readRecords() {
		return this.delegate.readRecords();
	}
}
