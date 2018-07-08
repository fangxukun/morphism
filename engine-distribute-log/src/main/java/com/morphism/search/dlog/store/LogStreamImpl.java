package com.morphism.search.dlog.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/31
 * Time: 15:37
 */
public class LogStreamImpl implements LogStream{
	private static final Logger				LOGGER				= LoggerFactory.getLogger(LogStreamImpl.class);
	private final StoreDirectory			storeDirectory;
	private final List<Path>				filePaths;
	private final LogRecord					logRecord;

	private LogStore						current;
	private long 							position;



	public LogStreamImpl(StoreDirectory directory,long timestamp) throws IOException {
		this.storeDirectory = directory;
		this.filePaths = storeDirectory.getFilePaths();
		this.logRecord = new LogRecord(4096);

		this.position = storeDirectory.seek(timestamp);
		if(position == -1){
			long oldestTimestamp = storeDirectory.oldestTimestamp();
			this.position = storeDirectory.seek(oldestTimestamp);
			LOGGER.warn("timestamp is can not reach,locate to the oldest timestamp:" + oldestTimestamp + " position:" + position);
		}

		position = Math.max(0,position);
		this.current = storeDirectory.locateStore(position);
	}


	@Override
	public LogRecord take() throws IOException, InterruptedException {
		long readPosition = -1;
		while(readPosition < 0){
			readPosition = current.take(position,logRecord);
			if(readPosition == LogStore.END_OF_FILE || readPosition == LogStore.CLOSED_FILE){
				storeDirectory.returnStore(current);
				this.current = storeDirectory.locateStore(position);
			}
		}

		this.position = readPosition;
		return logRecord;
	}

	@Override
	public void close() throws IOException {
		this.storeDirectory.returnStore(current);
	}
}

